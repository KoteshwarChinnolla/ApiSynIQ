import copy
import os
from typing import Union
import uuid
from dotenv import load_dotenv
from requests_toolbelt import user_agent
from typing_extensions import TypedDict,List,Dict,Any,Type 
from langgraph.graph import StateGraph, START, END
from langgraph.checkpoint.memory import InMemorySaver
import json, re
# from langchain_google_genai import ChatGoogleGenerativeAI
from pydantic import BaseModel, Field
from langchain.agents import create_agent 
from Querying.RestApi import RequestApi
from langchain.tools import tool, ToolRuntime 
from dataclasses import dataclass
from Processing.StringToPydantic import GeneratePydantic
from .prompts import API_FILLING_PROMPT
from Retrieval.FetchApi import stream
from .Tools import query
from .CheckPointer import checkpoint_exists, load_checkpoint, save_checkpoint, update_checkpoint
from Retrieval.data_pb2 import Text,AudioChunk
from Retrieval import data_pb2 as grpc_data
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage, AIMessageChunk
from langchain_groq import ChatGroq
from langchain_aws import ChatBedrockConverse
from langchain.agents.middleware import before_model, after_agent, after_model, AgentState
from langgraph.runtime import Runtime
from .Data import AgentType, decode_llm_output

load_dotenv()
# GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
os.environ["GROQ_API_KEY"] = os.getenv("GROQ_API_KEY")

aws_access_key_id = os.getenv("AWS_ACCESS_KEY_ID")
aws_secret_access_key = os.getenv("AWS_SECRET_ACCESS_KEY")
aws_region = os.getenv("AWS_REGION")
model = os.getenv("MODEL")


class Schema(BaseModel):
    """Each of variables contains the pydantic model for the patheter required by the API."""
    body: Any = None
    path: Any = None
    query: Any = None
    variables: Any = None
    headers: Any = None
    cookies: Any = None

# @dataclass
class configDetails(BaseModel):
    user_name: str= ""
    session_id: str = ""
    stream_id: str = ""


class StateObject(AgentState):
    messages: List[Dict[str, str]] = []
    markdown: str = "" 
    schema: Dict = None
    method: str = ""
    url: str = ""
    api_response: str = ""



@after_agent
def saveContext(state: AgentState, runtime: Runtime) -> None:
    # print(runtime.context, type(runtime.context))
    stream_id = runtime.context.stream_id
    update_checkpoint(state, stream_id, AgentType.SUB_AGENT)

class SubAgent:
    def __init__(self):

        # self.llm_model = ChatGoogleGenerativeAI(
        #     model="gemini-2.0-flash",
        #     google_api_key=GEMINI_API_KEY
        # )
        self.sample_model = ChatGroq(
            model="llama-3.3-70b-versatile"
        )

        self.llm_model = ChatBedrockConverse(
            # model="us.anthropic.claude-3-5-haiku-20241022-v1:0",
            # model_id=model,
            # provider="anthropic",
            # model="openai.gpt-oss-20b-1:0",
            model="qwen.qwen3-32b-v1:0",
            aws_access_key_id=aws_access_key_id,
            aws_secret_access_key=aws_secret_access_key,
            region_name=aws_region,
            system=[
                {
                    "text": API_FILLING_PROMPT
                },
                # {
                #     "cachePoint": {"type": "default"}
                # }
            ]
        )
        
        self.agent = create_agent(
            model=self.llm_model,
            # middleware=[saveContext],
            state_schema=StateObject,
            context_schema=configDetails,
            tools=[query]
        )

        self.sample_agent = create_agent(
            model=self.sample_model,
            state_schema=StateObject,
            context_schema=configDetails,
            system_prompt=SystemMessage(content=API_FILLING_PROMPT),
            tools=[query]
        )
    
    async def run_agent(self, message:str, config: configDetails, state: StateObject | None = None) -> None:

        if state is None:
            state = copy.deepcopy(StateObject(messages=[], markdown="", schema=None, method="GET", url=None))

        stream_id = config.stream_id
        
        if stream_id and checkpoint_exists(stream_id, AgentType.SUB_AGENT):

            print("✅ Checkpoint exists")

            saved = load_checkpoint(stream_id)
            if not saved:
                raise RuntimeError("No checkpoint found")
            state = copy.deepcopy(saved["state"][AgentType.SUB_AGENT])
        

        if state["markdown"] == "" or state["schema"] is None:
            print("✅ Hit")
            result = self.model_selector(message)
            state["markdown"] = result["model_card"]
            state["schema"] = result["schema"]
            state["method"] = result["method"]
            state["url"] = result["url"]
            SystemMessage = f"""
                Here are the schema and model card for the API to be filled:
                Schema: {result["schema"]}
                Model Card: {result["model_card"]}
            """
            state["messages"].append({
                "role": "system",
                "content": SystemMessage
            })

        
        state["messages"].append({
            "role": "user",
            "content": message
        })

        llm_output = ""

        try:
            async for event in self.sample_agent.astream(input=state, context=config, stream_mode="messages"):
                try:
                    text = decode_llm_output(event)

                    if not text:
                        continue

                    grpc_send = AudioChunk(
                        text=text,
                        username=config.user_name,
                        session_id=config.session_id,
                        stream_id=config.stream_id,
                        language="en-US",
                        options={"content": "text", "role": "subagent", "status": "pending"}
                    )

                    stream.push_audio_chunk(grpc_send)
                    llm_output += text
                    print(grpc_send)

                except Exception as e:
                    print("Decode error:", e)
                    print("Event:", event)

        except Exception as e:
            print("Error:", e)
        stream.flush()

        state["messages"].append({
            "role": "assistant",
            "content": llm_output
        })

        if stream_id:
            update_checkpoint(state, stream_id, AgentType.SUB_AGENT)

        return state
    
    def model_selector(self,user_input: str):

        gen=GeneratePydantic()

        user_input = user_input
        a = grpc_data.query(query=user_input, limit=3)
        fetched_results=gen.Fetch(a,"INPUT")

        if not fetched_results:
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
        llm_ans = self.sample_model.invoke(selection_prompt)
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
        path = selected_inputs.input.inputPathParams
        query = selected_inputs.input.inputQueryParams
        variables =selected_inputs.input.inputVariables
        headers =selected_inputs.input.inputHeaders
        cookies =selected_inputs.input.inputCookies

        schema = Schema(
            body=body.model_json_schema() if body else None,
            path=path.model_json_schema() if path else None,
            query=query.model_json_schema() if query else None,
            variables=variables.model_json_schema() if variables else None,
            headers=headers.model_json_schema() if headers else None,
            cookies=cookies.model_json_schema() if cookies else None
        ).__dict__

        method = (selected_inputs.httpMethod or "GET").upper()
        base_url = selected_inputs.globalPath.rstrip("/")
        endpoint = selected_inputs.endpoint.lstrip("/")
        url = f"{base_url}/{endpoint}"
        return {
            "method": method,
            "url": url,
            "name": selected_api,
            "model_card":selected_markdown,
            "schema": schema
        }
    

    async def run(self, chunk: Text, state: StateObject | None = None):
        config = configDetails(user_name=chunk.username, session_id=chunk.session_id, stream_id=chunk.stream_id)
        print("[VERIFICATION] received config", config)
        return await self.run_agent(message=chunk.text, config=config, state=state)
    
    