import os
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
from Generation.prompts import API_FILLING_PROMPT

load_dotenv()
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")

@dataclass
class Context:
  context_data:Dict

@tool
async def speak(text: str, runtime: ToolRuntime) -> str:
  """Takes i the text and waits for the user to respond.
  
  Args:
      text (str): The text to be spoken.
  Returns:
      str: The response from the user.
  """
  response = input(text)
  runtime.state.history.append({"input": text, "output": response})
  return response

@tool
async def query_api(results: Dict, runtime: ToolRuntime) -> Dict:
  """Takes in the Parmeters and returns the response from the API.
  
  Args:
      results (Dict): The parameters to be passed to the API.
  Returns:
      Dict: The response from the API.
  """
  return {"output": results}

class State(TypedDict):
    request: str
    markdown: str
    combined: Any
    history: List[Dict[str, str]]


class SubAgent:
    def __init__(self,context:Context):
        self.context=context
        self.llm_model=ChatGoogleGenerativeAI(model="gemini-2.0-flash",api_key=GEMINI_API_KEY)
        self.memory = InMemorySaver()
        self.graph = self._build_graph()
        self.schema = {}
    
    def _build_graph(self):
        builder = StateGraph(State)
        builder.add_node("agent", self.Agent)
        builder.add_edge(START, "agent")
        builder.add_edge("agent", END)
        return builder.compile(checkpointer=self.memory)
    
    def Agent(self,state: State):
        agent = create_agent(self.llm_model, tools=[speak, query_api], response_format=self.schema, interrupt_before=["query_api"])
        result = self.model_selector(user_input=getattr(state, "request", ""))
        prompt = API_FILLING_PROMPT(schema=result["schema"], model_card=result["model_card"])
        output = agent.invoke(input=prompt)
        print(output)

    def model_selector(self,user_input: str):

        gen=GeneratePydantic()

        user_input = user_input
        a = query(query=user_input, limit=3)
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
        llm_choice = self.llm_model.invoke(selection_prompt).content.strip()

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

        self.schema_classes[selected_api] = {
            "body":body,
            "params": params,
            "query": query,
            "variables": variables,
            "headers": headers,
            "cookies": cookies
        }
        self.object_store[selected_api]=selected_inputs
        return {
        "name": selected_api,
        "model_card":selected_markdown,
        "schema": {
            "inputBody": body.model_json_schema() if body else None,
            "inputPathParams": params.model_json_schema() if params else None,
            "inputQueryParams": query.model_json_schema() if query else None,
            "inputVariables":variables.model_json_schema() if variables else None,
            "inputHeaders":headers.model_json_schema() if headers else None,
            "inputCookies":cookies.model_json_schema() if cookies else None
            },
        }

