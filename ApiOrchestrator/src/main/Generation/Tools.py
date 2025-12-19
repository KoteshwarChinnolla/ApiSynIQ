
from typing import Any, Dict
from langchain.tools import tool, ToolRuntime
from langchain_core.messages import AIMessage, HumanMessage

from Querying.RestApi import RequestApi
from .CheckPointer import update_checkpoint 
from Retrieval.data_pb2 import Text,AudioChunk
from Retrieval.FetchApi import AudioStream
from Retrieval.FetchApi import stream

@tool(description="Use the query tool when the required parameters are filled. Make sure to pass the inputs correctly. the tool actually queries the API with the parameters.")
async def query(inputs: Dict, runtime: ToolRuntime) -> str:
    """Takes in the input Parameters and returns the response from the API.
    
    Args:
        inputs(Dict) : Final json in STRICT JSON MODE.
    Returns:
        Dict: The response from the API.
    """
    print("Tool call received: ", inputs)
    state = runtime.state
    state["pending_prompt"] = None
    query = RequestApi()
    query_response = query.query(inp=inputs, method=state["method"], url=state["url"])
    state["query_response"] = query_response
    text = str(query_response)
    
    if not text:
        return state

    grpc_send = AudioChunk(
        text=text,
        username=state["user_name"],
        session_id=state["session_id"],
        stream_id=state["stream_id"],
        language="en-US",
    )
    
    stream.push_audio_chunk(grpc_send)
    return state