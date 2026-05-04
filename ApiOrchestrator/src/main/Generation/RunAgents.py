import asyncio
from fastapi import FastAPI
from pydantic import BaseModel
from .SubAgent import SubAgent
from .DeepAgent import deep_agent
from Retrieval.data_pb2 import Text
import uvicorn
from Transcribe.STT.Factory import InitTextModelsWhisper
from Transcribe.TextToSpeech import InitVoiceModels
from .Data import AgentType


app = FastAPI()

# InitVoiceModels()
# InitTextModelsWhisper()

class normalizeText(BaseModel):
    text: str
    username: str
    session_id: str
    stream_id: str
    language: str
    options: dict
    
    def to_grpc(self):
        return Text(text=self.text, username=self.username, session_id=self.session_id, stream_id=self.stream_id, language=self.language, options=self.options)
    
subAgent = SubAgent()
deepAgent = deep_agent()

@app.post("/run_agent")
async def run_agent(chunk: normalizeText):
    chunk = chunk.to_grpc()
    role = chunk.options["role"]
    print("[VERIFICATION RUN AGENT] received role", role)
    if(role == "DEEP_AGENT"):
        await deepAgent.run(chunk=chunk, state=None)
    else:
        await subAgent.run(chunk=chunk, state=None)



if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=3000)
    # from Retrieval.GrpcServer import GrpcServer
    # asyncio.run(GrpcServer().serve())