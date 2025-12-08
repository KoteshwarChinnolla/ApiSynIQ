import os
import uuid
from dotenv import load_dotenv
from typing_extensions import TypedDict,List,Dict,Any,Type 
from langgraph.graph import StateGraph, START, END
from langgraph.checkpoint.memory import InMemorySaver
import json, re
from langchain_google_genai import ChatGoogleGenerativeAI
from pydantic import BaseModel, Field
from langchain.agents import create_agent 
from Querying.RestApi import RequestApi
from langchain.tools import tool, ToolRuntime 
from dataclasses import dataclass
from Processing.StringToPydantic import GeneratePydantic
from .prompts import API_FILLING_PROMPT, System_Prompt_Resolver, make_api_prompt
from Retrieval.FetchApi import AudioStream
from .Tools import speak, query_api
from .CheckPointer import checkpoint_exists, load_checkpoint, save_checkpoint, update_checkpoint
from Retrieval.data_pb2 import Text,AudioChunk
from Retrieval import data_pb2 as grpc_data
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage
# from langchain_groq import ChatGroq
from langchain_aws import ChatBedrockConverse
from langchain.agents.middleware import before_model, after_model, AgentState
from langgraph.runtime import Runtime


@after_model
def saveContext(state: AgentState, runtime: Runtime) -> dict[str, Any] | None:
    update_checkpoint(runtime, runtime.state["stream_id"])
    print(f"Model returned: {state['messages'][-1].content}")
    return None


load_dotenv()
# GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
# os.environ["GROQ_API_KEY"] = os.getenv("GROQ_API_KEY")

aws_access_key_id = os.getenv("AWS_ACCESS_KEY_ID")
aws_secret_access_key = os.getenv("AWS_SECRET_ACCESS_KEY")
aws_region = os.getenv("AWS_REGION")


class Schema(BaseModel):
    """Each of variables contains the pydantic model for the parameter required by the API."""
    body: Any = None
    param: Any = None
    query: Any = None
    variables: Any = None
    headers: Any = None
    cookies: Any = None

class Schema(BaseModel):
    """Each of variables contains the pydantic model for the parameter required by the API."""
    body: Any = None
    param: Any = None
    query: Any = None
    variables: Any = None
    headers: Any = None
    cookies: Any = None

@dataclass
class Context:
  context_data:Dict


class State(TypedDict):
    request: str = ""
    response: str = ""
    markdown: str = ""
    history: List[Dict[str, str]] = []
    pending_prompt: str = ""
    stream_id: str = ""
    user_name: str = ""
    session_id: str = ""
    context: Context = Context(context_data={})


class SubAgent:
    def __init__(self):

        # self.llm_model = ChatGoogleGenerativeAI(
        #     model="gemini-2.0-flash",
        #     google_api_key=GEMINI_API_KEY
        # )
        # self.llm_model = ChatGroq(
        #     model="openai/gpt-oss-120b"
        # )
        self.llm_model = ChatBedrockConverse(
            model="openai.gpt-oss-safeguard-20b",
            aws_access_key_id=aws_access_key_id,
            aws_secret_access_key=aws_secret_access_key,
            region_name=aws_region
        )
        
        self.graph = self._build_graph()

        self.agent = create_agent(
            model=self.llm_model,
            system_prompt=System_Prompt_Resolver,
            middleware=[saveContext]
        )


    def _build_graph(self) -> StateGraph:
        builder = StateGraph(State)
        builder.add_node("agent", self.Agent)
        builder.add_edge(START, "agent")
        builder.add_edge("agent", END)
        return builder.compile()
    
    async def Agent(self, state: State):

        user_input = state["request"]
        stream_id = state.get("stream_id")

        if stream_id and checkpoint_exists(stream_id):

            saved = load_checkpoint(stream_id)

            if not saved:
                raise RuntimeError("No checkpoint found")
            
            state = saved["state"]
            state["history"].append(
                {"input": state.get("pending_prompt"), "output": user_input})
            state.pop("pending_prompt", None)

        if state["context"] and state["context"].context_data.get("schema") is None:
            result = self.model_selector(state["request"])
            state["context"].context_data.update(result)
        else:
            result = state["context"].context_data


        SystemMessage = f"""
        Here are the schema and model card for the API to be filled:
        Schema: {result["schema"]}
        Model Card: {result["model_card"]}
        """
        messages = make_api_prompt(SystemMessage, state["history"], state["request"])
        llm_output = await self.llm_model.ainvoke(messages)
        print(f"\n🤖 LLM Output: {llm_output.content}")
        new_history = state["history"]
        new_history.append(("human", user_input))
        new_history.append(("ai", llm_output.content))

        state["response"] = llm_output.content
        state["markdown"] = result["model_card"]
        state["history"] = new_history
        state["pending_prompt"] = llm_output.content

        await update_checkpoint(state, stream_id)
        return state

    def model_selector(self,user_input: str):

        gen=GeneratePydantic()

        user_input = user_input
        a = grpc_data.query(query=user_input, limit=3)
        fetched_results=gen.Fetch(a,"RETURN")

        if not fetched_results:
            print("⚠️ No API matches found!")
            return {"selected_model": None}
        
        api_names = list(fetched_results.keys())
        inputs_=list(fetched_results.values())
        (api1,api2,api3),(inp1,inp2,inp3) =api_names,inputs_

        markdown1 = getattr(inp1, "markDown", "")
        markdown2 = getattr(inp2, "markDown", "")
        markdown3 = getattr(inp3, "markDown", "")

        selection_prompt = f"""
        You are an intelligent API selection system.
        User Query:
        {user_input}There are 3 API candidates below. Choose the BEST one that matches the user's intent.
        API 1: {api1} Description: {markdown1}
        API 2: {api2} Description: {markdown2}
        API 3: {api3} Description: {markdown3}
        RULES:Read the every API description and api carefully and choose the one that best fits the user's request.
        Respond with EXACTLY one of: 1, 2, or 3.Do NOT explain your answer.
        Just output the best matching API number.give me the best that suits the user query.
        """
        llm_ans = self.llm_model.invoke(selection_prompt)
        print(f"\n🤖 LLM selected API option: {llm_ans}")
        llm_choice = llm_ans.content

        if llm_choice == "1":
            selected_api = api1
            selected_inputs = inp1
            selected_markdown=markdown1

        elif llm_choice == "2":
            selected_api = api2
            selected_inputs = inp2
            selected_markdown=markdown2

        else:
            selected_api = api3
            selected_inputs = inp3
            selected_markdown=markdown3

        print(f"\n✅ Selected API: {selected_api}")
        body = selected_inputs.input.inputBody
        params = selected_inputs.input.inputPathParams
        query = selected_inputs.input.inputQueryParams
        variables =selected_inputs.input.inputVariables
        headers =selected_inputs.input.inputHeaders
        cookies =selected_inputs.input.inputCookies

        schema = Schema(
            body=body.model_json_schema() if body else None,
            params=params.model_json_schema() if params else None,
            query=query.model_json_schema() if query else None,
            variables=variables.model_json_schema() if variables else None,
            headers=headers.model_json_schema() if headers else None,
            cookies=cookies.model_json_schema() if cookies else None
        ).__dict__
        return {
            "name": selected_api,
            "model_card":selected_markdown,
            "schema": schema
        }
    


subAgent = SubAgent()
graph = subAgent.graph
async def run(chunk: Text) -> State:
    state = {
        "request": chunk.user_input,
        "stream_id  ": chunk.stream_id,
        "user_name": chunk.username,
        "session_id": chunk.session_id
    }
    return await graph.invoke(state)
