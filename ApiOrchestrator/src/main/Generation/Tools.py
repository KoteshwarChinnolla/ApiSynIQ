
from typing import Any, Dict
from langchain.tools import tool, ToolRuntime
from langchain_core.messages import AIMessage, HumanMessage
from .CheckPointer import update_checkpoint 
from Retrieval.data_pb2 import Text,AudioChunk
from Retrieval.FetchApi import AudioStream



@tool
async def speak(text: str, runtime: ToolRuntime) -> Dict:
    """Converts text to speech and streams it as audio chunks.
    Args:
        text (str): The text to be converted to speech.
    Returns:
        Dict: A dictionary containing the user response.
    """
    runtime.state["pending_prompt"] = text
    checkpoint_id = runtime.state["stream_id"]
    runtime.state["history"].append(AIMessage(content=text))

    await update_checkpoint(runtime, checkpoint_id)

    send_to_json = AudioChunk(
        audio_bytes=None,
        text=text,
        username=runtime.state["user_name"],
        session_id=runtime.state["session_id"],
        stream_id=runtime.state["checkpoint_id"],
        language="en-US",
        audio_option="",
        options={}
    )

    # AudioStream.push_chunk(send_to_json)
    print(send_to_json)
    return {"tool_output": send_to_json.__dict__}

async def query_api(results: Dict) -> Dict:
  """Takes in the Parmeters and returns the response from the API.
  
  Args:
      results (Dict): The parameters to be passed to the API.
  Returns:
      Dict: The response from the API.
  """
  return {"output": results}