import copy
import os
from deepagents import FilesystemMiddleware, create_deep_agent
from langchain.agents.middleware import TodoListMiddleware
from langchain_aws import ChatBedrockConverse
from Retrieval.FetchApi import stream
from .Data import AgentType, decode_llm_output
from .prompts import DEEP_AGENT_SYSTEM_PROMPT
from .Tools import DEEPAGENT_TOOLS
from langchain.agents import create_agent 
from typing_extensions import TypedDict,List,Dict,Any,Type 
from Processing.StringToPydantic import Inputs
from .CheckPointer import checkpoint_exists, load_checkpoint, save_checkpoint, update_checkpoint
from Retrieval.data_pb2 import Text,AudioChunk


deepagent_middleware = [
        TodoListMiddleware(),
        FilesystemMiddleware()]

aws_access_key_id = os.getenv("AWS_ACCESS_KEY_ID")
aws_secret_access_key = os.getenv("AWS_SECRET_ACCESS_KEY")
aws_region = os.getenv("AWS_REGION")
model = os.getenv("MODEL")

def deep_agent_config(BaseModel):
    user_name: str = ""
    session_id: str = ""
    stream_id: str = ""

def deep_agent_state(AgentState):
    messages : List[Dict[str, str]] = []
    apis : Dict[str, Inputs] = []


class deep_agent:
    def __init__(self):
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
                    "text": DEEP_AGENT_SYSTEM_PROMPT
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
        # self.deep_agent = create_deep_agent(self.agent, middleware=deepagent_middleware)

    async def Agent(self, messages: str, config: deep_agent_config, state: deep_agent_state = None | None):

        if state is None:
            state = copy.deepcopy(deep_agent_state(messages=[], apis={}))

        stream_id = config.stream_id
        if stream_id and checkpoint_exists(stream_id, AgentType.DEEP_AGENT):
            print("✅ Checkpoint exists")

            saved = load_checkpoint(stream_id)
            if not saved:
                raise RuntimeError("No checkpoint found")
            state = copy.deepcopy(saved["state"][AgentType.DEEP_AGENT])
        config_details = {"role":"system", "content":f"Session_id: {config.session_id} and stream_id: {stream_id}"}
        state[messages].extend([{"role": "user", "content":messages}, config_details])

        llm_output = ""

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
