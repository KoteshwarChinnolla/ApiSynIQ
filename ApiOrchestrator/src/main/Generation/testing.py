from Retrieval.data_pb2 import query
from Retrieval.GrpcServer import GrpcServer
from Transcribe.TextToSpeech import InitVoiceModels
from Transcribe.SpeechToText import InitTextModels

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
        
    InitVoiceModels()
    InitTextModels()
    GrpcServer()
