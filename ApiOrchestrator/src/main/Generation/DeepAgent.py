import copy
import os
from typing import Annotated, Literal
from deepagents import FilesystemMiddleware, create_deep_agent
from langchain.agents.middleware import TodoListMiddleware
from langchain_aws import ChatBedrockConverse
from pydantic import BaseModel
from Retrieval.FetchApi import stream
from .Data import AgentType, decode_llm_output
from .prompts import DEEP_AGENT_SYSTEM_PROMPT, FRONT_GATE_SYSTEM_PROMPT, SILENT_ORCHESTRATOR_PROMPT
from .DeepAgentTools import DEEPAGENT_TOOLS, GET_API_TOOL, FILE_SYSTEM_TOOLS, api_resolver
from langchain.agents import create_agent 
from typing_extensions import TypedDict,List,Dict,Any,Type ,Annotated
from Processing.StringToPydantic import Inputs
from .CheckPointer import checkpoint_exists, load_checkpoint, save_checkpoint, update_checkpoint
from Retrieval.data_pb2 import Text,AudioChunk
from langchain.agents.middleware import before_model, after_agent, after_model, AgentState
from langchain.messages import AnyMessage
import operator
from langchain_core.messages import AIMessage, HumanMessage, ToolMessage
from langgraph.graph import StateGraph, START, END
import pandas as pd
from io import StringIO
from langgraph.runtime import Runtime
from langgraph.checkpoint.memory import InMemorySaver
import json, re
from .SubAgent import SubAgent
from langgraph.prebuilt import tools_condition
from langgraph.prebuilt.tool_node import ToolNode

aws_access_key_id = os.getenv("AWS_ACCESS_KEY_ID")
aws_secret_access_key = os.getenv("AWS_SECRET_ACCESS_KEY")
aws_region = os.getenv("AWS_REGION")
model = os.getenv("MODEL")

class deep_agent_config(BaseModel):
    user_name: str = ""
    session_id: str = ""
    stream_id: str = ""

class deep_agent_state(AgentState):
    messages: Annotated[list[AnyMessage], operator.add]
    apis : Dict[str, Inputs] = []
    plan: str = ""


class deep_agent:
    def __init__(self):
        self.llm_model = ChatBedrockConverse(
            # model="us.anthropic.claude-3-5-haiku-20241022-v1:0",
            # model_id=model,
            # provider="anthropic",
            model="openai.gpt-oss-120b-1:0",
            # model="qwen.qwen3-32b-v1:0",
            aws_access_key_id=aws_access_key_id,
            aws_secret_access_key=aws_secret_access_key,
            region_name=aws_region,
            system=[
                {
                    "text": DEEP_AGENT_SYSTEM_PROMPT
                },
                # {
                #     "cachePoint": {"type": "default"}
                # }
            ]
        )
        self.front_gate_agent = self.llm_model.bind_tools([GET_API_TOOL])

        self.checkpoint = InMemorySaver()
        self.agent = (
            StateGraph(state_schema=deep_agent_state, context_schema=deep_agent_config)
            .add_node("front_gate", self.front_gate)
            .add_node("front_gate_tools", ToolNode([GET_API_TOOL]))
            .add_node("orchestrator", self.orchestrator)
            .add_node("api_resolver", SubAgent)
            .add_edge(START, "front_gate")
            .add_conditional_edges("front_gate", tools_condition, {
                "tools": "front_gate_tools",
                "__end__": END
            })
            .add_edge("front_gate_tools", "orchestrator")
            .add_conditional_edges("orchestrator", resolver_condition, {
                "resolve": "api_resolver",
                "__end__": END
            })
            .add_edge("api_resolver", "orchestrator")
            .add_edge("orchestrator", "front_gate")
            .compile(checkpointer=self.checkpoint)
        )
        self.front_gate_model = ChatBedrockConverse(
            model="openai.gpt-oss-120b-1:0",
            # model="qwen.qwen3-32b-v1:0",
            aws_access_key_id=aws_access_key_id,
            aws_secret_access_key=aws_secret_access_key,
            region_name=aws_region,
            system=[
                {
                    "text": FRONT_GATE_SYSTEM_PROMPT
                },
                # {
                #     "cachePoint": {"type": "default"}
                # }
            ]
        )

        self.orchestrator_model = ChatBedrockConverse(
            model="openai.gpt-oss-120b-1:0",
            # model="qwen.qwen3-32b-v1:0",
            aws_access_key_id=aws_access_key_id,
            aws_secret_access_key=aws_secret_access_key,
            region_name=aws_region,
            system=[
                {
                    "text": SILENT_ORCHESTRATOR_PROMPT
                },
                # {
                #     "cachePoint": {"type": "default"}
                # }
            ]
        )

        self.agent = create_agent(model=self.llm_model,
                                  tools=DEEPAGENT_TOOLS,
                                  state_schema=deep_agent_state,
                                  context_schema=deep_agent_config
                                  )
        self.orchestrator_agent = create_agent(model=self.orchestrator_model,
                                  tools=FILE_SYSTEM_TOOLS,
                                  state_schema=deep_agent_state,
                                  context_schema=deep_agent_config,
                                  )
        self.front_gate_agent = create_agent(model=self.front_gate_model,
                                  tools=GET_API_TOOL,
                                  state_schema=deep_agent_state,
                                  context_schema=deep_agent_config,
                                  )
    async def build_agent(self):
        agent_builder = StateGraph(state_schema=deep_agent_state, context_schema=deep_agent_config)
        agent_builder.add_node("front_gate", self.front_gate)
        agent_builder.add_node("orchestrator", self.orchestrator)
        agent_builder.add_node("resolver", self.resolver)

    async def front_gate(self, state=deep_agent_state):
        state = self.update_plan_in_state_message(state)
        result = self.front_gate_agent.invoke(state=state)
        state["messages"].append({"role": "assistant", "content":result.content})
        return state
    
    async def should_orchestrate(state: deep_agent_state) -> Literal["orchestrator", "end"]:
        messages = state["messages"]
        last_message = messages[-1]

        if type(last_message)==ToolMessage:
            return "orchestrator"
        
        return "end"

    async def orchestrator(self, State: deep_agent_state) -> deep_agent_state:
        state = self.update_plan_in_state_message(State)
        result = self.orchestrator_agent.invoke(state=state)
        return state
        
    async def resolver(self, state: deep_agent_state,  runtime: Runtime) -> deep_agent_state:
        df = pd.read_csv(StringIO(state["plan"]))
        for i in range(len(df)):
            state = await api_resolver(task=df["TASK"][i], selected_api=df["API"][i], state=state, config=runtime.context)
        return state


    async def update_plan_in_state_message(state: deep_agent_state):
        if "plan" not in state:
            return state
        if "messages" not in state:
            state["messages"] = []

        state["messages"] = [
            msg for msg in state["messages"]
            if not (msg["role"] == "assistant" and msg.get("is_plan"))
        ]

        # Add updated plan
        state["messages"].append({
            "role": "assistant",
            "content": "Current plan:" + state["plan"],
            "is_plan": True
        })

        return state
    
    async def Agent(self, messages: str, config: deep_agent_config, state: deep_agent_state | None = None):

        if state is None:       
            state = copy.deepcopy(deep_agent_state(messages=[], apis={}))

        stream_id = config.stream_id
        if stream_id and checkpoint_exists(stream_id):
            print("✅ Checkpoint exists")

            saved = load_checkpoint(stream_id)
            if not saved:
                raise RuntimeError("No checkpoint found")
            state = copy.deepcopy(saved["state"][AgentType.DEEP_AGENT.name])
        config_details = {"role":"system", "content":f"name: {config.user_name}, Session_id: {config.session_id} and stream_id: {stream_id}"}
        state["messages"].extend([{"role": "user", "content":messages}, config_details])

        llm_output = ""
        print("[DEEPAGENT] sending messages", state["messages"])

        try:
            async for event in self.agent.astream(input=state, context=config, stream_mode="messages"):
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
                        options=dict(
                            content="text",
                            role=AgentType.DEEP_AGENT.name,
                            status="pending"
                        )
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
            update_checkpoint(state, stream_id, AgentType.DEEP_AGENT.name)

        return state
    
    async def run(self, chunk: Text, state: deep_agent_state | None = None):
        config = deep_agent_config(user_name=chunk.username, session_id=chunk.session_id, stream_id=chunk.stream_id)
        print("[VERIFICATION DEEP AGENT] received config", config)
        try:
            return await self.Agent(messages=chunk.text, config=config, state=state)
        except Exception as e:
            print("Error:", e)


