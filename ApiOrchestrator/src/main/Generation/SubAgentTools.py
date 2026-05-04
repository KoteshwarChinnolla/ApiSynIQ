from typing import Annotated, Any, Dict, Literal
from langchain.tools import tool, ToolRuntime
from langchain_core.messages import AIMessage, HumanMessage, ToolMessage
from pydantic import BaseModel
from Retrieval.FetchApi import AudioStream
from Retrieval.FetchApi import stream
from Retrieval.data_pb2 import Text,AudioChunk
from Querying.RestApi import RequestApi
from .prompts import QUERY_TOOL_PROMPT
from langgraph.types import Command


queries = RequestApi()


@tool(description=QUERY_TOOL_PROMPT)
def query(inputs: Dict, runtime: ToolRuntime) -> str:
    """Takes in the input Parameters and returns the response from the API.
    
    Args:
        inputs(Dict) : Final json in that is verified and matches the pydantic schema.
    Returns:
        str: The response from the API.
    """
    if not inputs:
        raise ValueError("It is not possible to call the API with empty inputs.")

    
    print("Tool call received: ", inputs) 
    state = runtime.state
    query_response = queries.query(inp=inputs, method=state["method"], url=state["url"])
    state["query_response"] = query_response
    print("Result of API call: ", query_response)
    text = str(query_response)
    
    if not text:
        return ""
    config = runtime.context

    grpc_send = AudioChunk(
        text=text,
        username=config.user_name,
        session_id=config.session_id,
        stream_id=config.stream_id,
        language="en-US",
        options={
            "content": "text",
            "role": "query_tool",
            "status": "success",
        }
    )
    
    # stream.push_audio_chunk(grpc_send)
    # stream.flush()
    return Command(
        update={
            "api_response": query_response,
            "messages": [ToolMessage(content=text, tool_call_id=runtime.tool_call_id)],
        },
    )

