from enum import Enum
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage, AIMessageChunk, AnyMessage, ToolMessage
from pydantic import BaseModel, Field


class AgentType(Enum):
    DEEP_AGENT = 1
    SUB_AGENT = 2

class Plan(BaseModel):
    api_name: str = Field(..., description="API name selected from get_apis")
    execution_reason: str = Field(..., description="Detailed reason for the API call")
    execution_status: str = Field(..., description="Status of the API call (planned, in_progress, completed, failed)")
    api_response: str = Field(..., description="Response from the API call")

def _render_message_chunk(token: AIMessageChunk) -> None:
    if token.text:
        return token.text
    if token.tool_call_chunks:
        return "\n" + token.tool_call_chunks


def _render_completed_message(message: AnyMessage) -> None:
    if isinstance(message, AIMessage) and message.tool_calls:
        return "\n" + f"Tool calls: {message.tool_calls}"
    if isinstance(message, ToolMessage):
        return "\n" + f"Tool response: {message.content_blocks}"

def stream_decode(data, stream_mode):
    if stream_mode == "messages":
        token, metadata = data
        if isinstance(token, AIMessageChunk):
            return _render_message_chunk(token)  
    if stream_mode == "updates":
        for source, update in data.items():
            if source in ("model", "tools"):  # `source` captures node name
                return _render_completed_message(update["messages"][-1])  

def decode_llm_output(event) -> str | None:
    if "model" in event:
        event = event["model"]
    if "messages" in event:
        event = event["messages"]
    if isinstance(event, tuple):
        event = event[0]

    # No stream, Direct invoke
    if isinstance(event, AIMessage):
        content = event.content

        if isinstance(content, str) and content.strip():
            return content

        if isinstance(content, list):
            texts = []
            for block in content:
                if block.get("type") == "text":
                    txt = block.get("text", "")
                    if txt.strip():
                        texts.append(txt)

            return "".join(texts) if texts else None

        return ""

    # Stream mode
    if isinstance(event, AIMessageChunk):
        content = event.content

        if not content:
            return ""

        # Groq case
        if isinstance(content, str) and content.strip():
            return content

        # Bedrocks case
        if isinstance(content, list):
            texts = []
            for block in content:
                block_type = block.get("type")

                if block_type == "text":
                    txt = block.get("text", "")
                    if txt.strip():
                        texts.append(txt)

            return "".join(texts) if texts else ""
        return ""
    return ""



