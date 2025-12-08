
from fastapi import FastAPI
from pydantic import BaseModel
from .SubAgent import State, SubAgent, Context
from Retrieval.data_pb2 import Text
import uvicorn

app = FastAPI()


class Input(BaseModel):
    text: str
    username: str
    session_id: str
    stream_id: str
    language: str
    options: dict
    
    def toGrpc(self) -> Text:
        return Text(text=self.text, username=self.username, session_id=self.session_id, stream_id=self.stream_id, language=self.language, options=self.options)


subAgent = SubAgent()
graph = subAgent.graph


async def run(chunk: Text) -> State:
    state = {
        "request": chunk.text,
        "history": [],
        "stream_id  ": chunk.stream_id,
        "user_name": chunk.username,
        "session_id": chunk.session_id,
        "context": Context(context_data={})
    }
    return await graph.ainvoke(state)

@app.post("/run")
async def run_agent(chunk: Input):
    chunk_grpc = chunk.toGrpc()
    result = await run(chunk=chunk_grpc)
    return result


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=3000)