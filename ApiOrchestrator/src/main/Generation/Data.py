from enum import Enum
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage, AIMessageChunk


class AgentType(Enum):
    DEEP_AGENT = 1
    SUB_AGENT = 2

def decode_llm_output(self, event) -> str | None:
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

        return None

    # Stream mode
    if isinstance(event, AIMessageChunk):
        content = event.content

        if not content:
            return None

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

            return "".join(texts) if texts else None
        return None
    return None
        