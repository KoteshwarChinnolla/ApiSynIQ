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

subAgent = SubAgent()
deepAgent = deep_agent()

async def run_agent(chunk: Text, state: dict | None = None):
    role = chunk.options["role"]
    print("[VERIFICATION RUN AGENT] received role", role)
    if(role == "DEEP_AGENT"):
        await deepAgent.run(chunk=chunk, state=state)
    else:
        await subAgent.run(chunk=chunk, state=state)



if __name__ == "__main__":
    # uvicorn.run(app, host="0.0.0.0", port=3000)
    from Retrieval.GrpcServer import GrpcServer
    asyncio.run(GrpcServer().serve())