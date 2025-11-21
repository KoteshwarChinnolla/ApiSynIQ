from Processing.StringToPydantic import GeneratePydantic
from Retrieval.data_pb2 import query
from Querying.RestApi import RequestApi
from Generation.Speak import PiperTTS

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

gen = GeneratePydantic()
output = gen.Fetch(query)
for key, value in output.items():
    print(value.markDown)
    for attr in attrs:
        val = getattr(value.input, attr)
        print(attr, val)
        if(val):
            inspect_model(val)
print("Choose between 0 to "+ str(len(output)-1))
re = RequestApi()
res = re.execute_input_request(list(output.values())[int(input())])
tts_system = PiperTTS(model_path="./Generation/Utils/en_GB-semaine-medium.onnx")

text = """
Go 1.22 brings two enhancements to the net/http package’s router: method matching and wildcards. 
These features let you express common routes as patterns instead of Go code. 
Although they are simple to explain and use, it was a challenge to come up with the right rules for selecting the winning pattern when several match a request.
"""
for token in text.split():
    tts_system.speak(token)
tts_system.shutDown()



