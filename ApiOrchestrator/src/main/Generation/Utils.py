from .SubAgent import SubAgent
from Retrieval.data_pb2 import Text

subAgent = SubAgent()

async def run_sub_agent(chunk: Text, state: dict | None = None):
    return await subAgent.run(chunk=chunk, state=state)
