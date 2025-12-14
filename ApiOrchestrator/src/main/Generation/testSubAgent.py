
import asyncio
from fastapi import FastAPI
from pydantic import BaseModel
from .SubAgent import State, SubAgent, Context
from Retrieval.data_pb2 import Text
import uvicorn
from Retrieval.data_pb2 import query
from Retrieval.GrpcServer import GrpcServer
from Transcribe.STT.Factory import InitTextModelsWhisper
from Transcribe.TextToSpeech import InitVoiceModels

app = FastAPI()

# InitVoiceModels()
# InitTextModelsWhisper()


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

async def run(chunk: Text) -> State:
    state = {
        "request": chunk.text,
        "history": [],
        "stream_id": chunk.stream_id,
        "user_name": chunk.username,
        "session_id": chunk.session_id,
        "context": Context(context_data={})
    }
    return await subAgent.graph.ainvoke(state)

@app.post("/run")
async def run_agent(chunk: Input):
    chunk_grpc = chunk.toGrpc()
    result = await run(chunk=chunk_grpc)
    return result


def inspect_model(model):
    print("\n=== CLASS INFO ===")
    print("Name:", model.__name__)
    print("Module:", model.__module__)
    print("Bases:", model.__bases__)

    print("\n=== FIELDS ===")
    for name, field in model.model_fields.items():
        print(f"Field: {name}")
        print(f"  Type: {field.annotation}")
        print(f"  Default: {field.default}")
        print(f"  Description: {field.description}")
        print(f"  Example: {field.examples}")

    print("\n=== ANNOTATIONS ===")
    print(model.__annotations__)

    print("\n=== JSON SCHEMA ===")
    print(model.model_json_schema())
  

attrs = {"inputBody", "inputPathParams", "inputQueryParams", "inputVariables", "inputHeaders", "inputCookies"}
query = query(query="get calender details", limit=2)

# gen = GeneratePydantic()
# output = gen.Fetch(query)
# for key, value in output.items():
#     print(value.markDown)
#     for attr in attrs:
#         val = getattr(value.input, attr)
#         print(attr, val)
#         if(val):
#             inspect_model(val)
# print("Choose between 0 to "+ str(len(output)-1))
# re = RequestApi()
# res = re.execute_input_request(list(output.values())[int(input())])




if __name__ == "__main__":
    # uvicorn.run(app, host="0.0.0.0", port=3000)
    asyncio.run(GrpcServer().serve())
    
    
    
#     let say i have a stateless service that actually takes in the input and stream the output to a front end page so it is a long running task keeping this in picture, i would like to make it concurent or paralel what ever you say, when ever multiple request come from the frontend api this 2 requests must not stay in the waiting stage so it must exicute stream paralley or cocurently. if i have to discribe the use case there is a model wich is hosted in the cloud (aws) there is a application that requesting the api for every single request, this model might take time to process(but it is not a cpu intensive) it is actually a waiting relates as the model is hosted in the cloud.so every request hits this if it waits then no point of application each user needs to wait a lot of time.

# so what solution fits here
# id it multi threading can we actually send each user to a suppurate thread ??
# or multiprocessing can we actually process each user in a suppurate core (i think this is in efficient)
# or asynchronous programming can we actually pass it and dont wait for the output
# which solution fits here
