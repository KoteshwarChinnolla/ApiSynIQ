import json
from Retrieval.FetchApi import FetchApi
from Retrieval.data_pb2 import query
from typing import Annotated, Literal, List, Any, Optional
from pydantic import BaseModel, Field

PY_TYPE_MAP = {
    "int": "int",
    "string": "str",
    "bool": "bool",
    "float": "float",
    "double": "float",
    "long": "int",
    "LocalDate": "str",        # or `datetime.date` if needed
    "LocalDateTime": "str",
}
class params():
    value: Any
class ReturnInputs():
    inputBody : Any
    inputPathParams : List[Any]
    inputQueryParams : List[Any] 
    inputVariables : List[Any] 
    inputHeaders : List[Any] 
    inputCookies : List[Any] 

class GeneratePydantic():
    Object : Any
    def __init__(self):
        self.fetchApi = FetchApi()

    def BuildPydantic(self):
        inputs = self.Object.inputs
        gen_inputs = ReturnInputs()
        DtoSchemas = self.Object.dtoSchemas or {}
        inputBody = inputs.inputBody
        inputPathParams = inputs.inputPathParams
        inputQueryParams = inputs.inputQueryParams
        inputVariables = inputs.inputVariables
        inputHeaders = inputs.inputHeaders
        inputCookies = inputs.inputCookies
        outputBody = self.Object.outputBody
        inputDescribe = self.Object.inputsDescribe
        inputBodyDescribe = inputDescribe.inputBody
        inputPathParamsDescribe = inputDescribe.inputPathParams 
        inputQueryParamsDescribe = inputDescribe.inputQueryParams
        inputVariablesDescribe = inputDescribe.inputVariables
        inputHeadersDescribe = inputDescribe.inputHeaders
        inputCookiesDescribe = inputDescribe.inputCookies
        
        if(inputBody and inputBodyDescribe):
            inputBody = json.loads(list(inputBody.items())[0][1])
            inputBodyRef = json.loads(list(inputBodyDescribe.items())[0][1])
            inputBodyDescribe = DtoSchemas.get(str(inputBodyRef), {})
            model_cls, code = self.generate_pydantic_class(inputBodyDescribe)
            gen_inputs.inputBody = model_cls


        # if(inputPathParams and inputPathParamsDescribe):
        #     inputPathParams = json.loads(list(inputPathParams.items())[0][1])
        #     inputPathParamsRef = json.loads(list(inputPathParamsDescribe.items())[0][1])
        #     inputPathParamsDescribe = DtoSchemas.get(str(inputPathParamsRef), {})
        #     model_cls, code = self.generate_pydantic_class(inputPathParamsDescribe)
        #     inputs.inputPathParams = model_cls

        # if(inputQueryParams and inputQueryParamsDescribe):
        #     inputQueryParams = json.loads(list(inputQueryParams.items())[0][1])
        #     inputQueryParamsRef = json.loads(list(inputQueryParamsDescribe.items())[0][1])
        #     inputQueryParamsDescribe = DtoSchemas.get(str(inputQueryParamsRef), {})
        #     model_cls, code = self.generate_pydantic_class(inputQueryParamsDescribe)
        #     inputs.inputQueryParams = model_cls

        # if(inputVariables and inputVariablesDescribe):
        #     inputVariables = json.loads(list(inputVariables.items())[0][1])
        #     inputVariablesRef = json.loads(list(inputVariablesDescribe.items())[0][1])
        #     inputVariablesDescribe = DtoSchemas.get(str(inputVariablesRef), {})
        #     model_cls, code = self.generate_pydantic_class(inputVariablesDescribe)
        #     inputs.inputVariables = model_cls

        # if(inputHeaders and inputHeadersDescribe):  
        #     inputHeaders = json.loads(list(inputHeaders.items())[0][1])
        #     inputHeadersRef = json.loads(list(inputHeadersDescribe.items())[0][1])
        #     inputHeadersDescribe = DtoSchemas.get(str(inputHeadersRef), {})
        #     model_cls, code = self.generate_pydantic_class(inputHeadersDescribe)
        #     inputs.inputHeaders = model_cls

        # if(inputCookies and inputCookiesDescribe):
        #     inputCookies = json.loads(list(inputCookies.items())[0][1])
        #     inputCookiesRef = json.loads(list(inputCookiesDescribe.items())[0][1])
        #     inputCookiesDescribe = DtoSchemas.get(str(inputCookiesRef), {})
        #     model_cls, code = self.generate_pydantic_class(inputCookiesDescribe)
        #     inputs.inputCookies = model_cls
        return gen_inputs

    def generate_pydantic_class(self,dto):
        print("dto: ", dto)
        class_name = dto.name
        fields = dto.fields

        lines = []
        imports = [
            "from pydantic import BaseModel, Field",
            "from typing import Optional, List, Literal",
        ]

        # Start class
        lines.append(f"class {class_name}(BaseModel):")
        lines.append(f"    \"\"\"{dto.description.strip()}\"\"\"")

        for f in fields:
            fname = f.name or ""
            dtype_raw = f.dataType or "string"
            description = f.description or ""
            example = f.example or ""
            default = f.defaultValue or "null"
            options = f.options or []

            # Convert type name
            py_type = PY_TYPE_MAP.get(dtype_raw, "str")

            # Literal support if options exist (comma-separated)
            if options:
                opts = [o.strip() for o in options.split(",")]
                literal_list = ", ".join([f'"{o}"' for o in opts])
                py_type = f"Literal[{literal_list}]"

            # Compose Field(...)
            field_params = []
            if description:
                field_params.append(f'description="{description}"')
            if example:
                try:
                    example_val = json.loads(example)
                except:
                    example_val = example
                # field_params.append(f"example={repr(example_val)}")

            if default not in (None, "", "null"):
                try:
                    default_val = json.loads(default)
                except:
                    default_val = default
                default_code = repr(default_val)
            else:
                default_code = "None"
                py_type = f"Optional[{py_type}]"

            field_code = f"Field({default_code}, {', '.join(field_params)})"
            example = f"# Example: {example or ''}"

            lines.append(f"    {fname}: {py_type} = {field_code} {example}")

        # Join everything
        class_code = "\n".join(imports) + "\n\n" + "\n".join(lines)

        # Execute to get actual class object
        local_vars = {}
        # print("class_code: ", class_code)
        exec(class_code, globals(), local_vars)
        clazz = local_vars[class_name]

        return class_code, clazz
    
    def TransformToMarkDown(self):
        name = self.Object.name
        endpoint = self.Object.endpoint
        httpMethod = self.Object.httpMethod
        description = self.Object.description
        returnDescription = self.Object.returnDescription
        autoExecute = self.Object.autoExecute

        inputs = self.Object.inputs
        outputBody = self.Object.outputBody

        inputBody = inputs.inputBody
        inputPathParams = inputs.inputPathParams
        inputQueryParams = inputs.inputQueryParams
        inputVariables = inputs.inputVariables
        inputHeaders = inputs.inputHeaders
        inputCookies = inputs.inputCookies


        markdown = f"""# {name}

    **Method:** `{httpMethod}`  **Endpoint:** `{endpoint}`  


    **Is it has to be executed automatically:** `{autoExecute}`  

    ---

    ## ⚙️ Inputs

    **Description:** {description}  
    """


        if inputPathParams:
            markdown += """
    ### 🔸 Path Parameters
    """
            for key, value in list(inputPathParams.items()):
                markdown += f" {key} : {value} \n"

        if inputQueryParams:
            markdown += """
    ### 🔸 Query Parameters
    """
            for key, value in list(inputQueryParams.items()):
                markdown += f" {key} : {value} \n"
        
        if inputVariables:
            markdown += """
    ### 🔸 Variables
    """
            for key, value in list(inputVariables.items()):
                markdown += f" {key} : {value} \n"

        if inputHeaders:
            markdown += """
    ### 🔸 Headers
    """
            for key, value in list(inputHeaders.items()):
                markdown += f" {key} : {value} \n"

        if inputCookies:
            markdown += """
    ### 🔸 Cookies
    """
            for key, value in list(inputCookies.items()):
                markdown += f" {key} : {value} \n"

        if inputBody:
            json_str = list(inputBody.items())[0][1]
            parsed = json.loads(json_str)
            inputBody_pretty = json.dumps(parsed, indent=4, ensure_ascii=False)
            markdown += f"""
    ### 🔸 Request Body
    ```json
    {inputBody_pretty}
    ```
    ---
    """
        markdown += """
    ## 📦 Response
    """

        if returnDescription:
            markdown += f"""
    **Description**: {returnDescription}
    """
            
        if outputBody:
            outputBody = outputBody
            parsed_output = json.loads(outputBody)
            outputBody_pretty = json.dumps(parsed_output, indent=4, ensure_ascii=False)
            markdown += f"""
    ### 🔸 Response Body
    ```json
    {outputBody_pretty}
    ```
    """
        return markdown

    def Fetch(self, query: query):
        ListOfObjects = self.fetchApi.searchMatchesForInputDescription(query)
        for repeated in ListOfObjects.inputs:
            self.Object = repeated
            self.BuildPydantic()
            mark_down = self.TransformToMarkDown()
        return mark_down


query = query(query="Attendance post and get", limit=2)
gen = GeneratePydantic()
mark_down = gen.Fetch(query)
print("Done")