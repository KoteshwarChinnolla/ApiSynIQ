import json
from typing import Any, List, Optional, Literal
from pydantic import BaseModel, Field
from Retrieval.FetchApi import FetchApi
from Retrieval.data_pb2 import query, Inputs
from Retrieval import data_pb2
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Dict
import threading
import time

# Java to Python type mapping

PY_TYPE_MAP = {
    "int": "int",
    "string": "str",
    "bool": "bool",
    "float": "float",
    "double": "float",
    "long": "int",
    "LocalDate": "str",
    "LocalDateTime": "str",
}


# Input structure for generated Pydantic classes

class ReturnInputs:
    inputBody: Any = None
    inputPathParams: Any = None
    inputQueryParams: Any = None
    inputVariables: Any = None
    inputHeaders: Any = None
    inputCookies: Any = None

    def __init__(self, **kwargs):
        for key, value in kwargs.items():
            setattr(self, key, value)

    def grpcToReturnInputs(self, inputs: data_pb2.Inputs):
        return ReturnInputs(
            inputBody=inputs.inputBody,
            inputPathParams=inputs.inputPathParams,
            inputQueryParams=inputs.inputQueryParams,
            inputVariables=inputs.inputVariables,
            inputHeaders=inputs.inputHeaders,
            inputCookies=inputs.inputCookies,
        )


# The python Object to hold all inputs and metadata, it is finally returned as the class output

class Inputs:
    name: str
    endpoint: str
    httpMethod: str
    description: str
    returnDescription: str
    autoExecute: bool
    input: ReturnInputs
    output: Any
    globalPath: str
    markDown: str
    example: ReturnInputs



class GeneratePydantic:

    def __init__(self, max_workers: int = 10):

        self.fetchApi = FetchApi() # Initialize FetchApi instance, this is about Grpc connection and data retreval
        self._class_cache = {} # Cache for generated Pydantic classes, to avoid redundant generation
        self._cache_lock = threading.Lock() # Lock for thread-safe cache access
        self._section_executor = ThreadPoolExecutor(max_workers=max_workers) # Executor for concurrent section processing



    def _process_section(self, section, sectionDescribe, target_attr, schemas, gen_inputs):
        """
        Generic handler for input sections such as:
        - Path Params
        - Query Params
        - Headers
        - Variables
        """
        if not (section and sectionDescribe):
            return

        section_refs = list(sectionDescribe.keys())

        if not section_refs:
            print(f"No keys found in describe for {target_attr}")
            return

        fields_list = []
        for ref in section_refs:
            schema = schemas.get(str(ref), None)
            if schema is None:
                print(f"Schema not found for section key: {ref}")
                continue
            fields_list.append(schema)

        if not fields_list:
            print(f"No valid schemas found for section: {target_attr}")
            return

        # Build Describe object with ALL fields
        des = {
            "name": target_attr,
            "description": None,
            "fields": tuple(fields_list)
        }

        # Generate class
        _, clazz = self.generate_pydantic_class(des)


        setattr(gen_inputs, target_attr, clazz)
        return

    def pydanticForBody(self, bodyData, DtoSchemas):
        """
        Create Pydantic class for request body.
        """
        if not bodyData:
            return None
        try:
            body_ref = json.loads(list(bodyData.items())[0][1])
        except:
            return None
        body_schema = DtoSchemas.get(str(body_ref), None)
        if not body_schema:
            return None
        _, clazz = self.generate_pydantic_class(body_schema)
        print(f"Generated Pydantic class for body: {clazz}")
        del body_schema
        return clazz


    def BuildPydanticForInputs(self, obj) -> ReturnInputs:
        Object = obj
        gen_inputs = ReturnInputs()
        
        inputs = Object.inputs
        describe = Object.inputsDescribe
        paramSchemas = Object.describeDtosForParms
        schemas = Object.dtoSchemas
        tasks = []

        def task_runner(section, sectionDescribe, target_attr):
            tmp_gen = ReturnInputs()
            if target_attr == "inputBody":
                tmp_gen.inputBody = self.pydanticForBody(section, sectionDescribe)
            else:
                self._process_section(section, sectionDescribe, target_attr, paramSchemas, tmp_gen)
            # print(f"Completed processing for {target_attr} and waiting to return... {threading.current_thread().name}")
            # time.sleep(5)
            return target_attr, getattr(tmp_gen, target_attr)

        exe = self._section_executor
        futures = [
            exe.submit(task_runner, describe.inputBody, schemas, "inputBody"),
            exe.submit(task_runner, inputs.inputPathParams, describe.inputPathParams, "inputPathParams"),
            exe.submit(task_runner, inputs.inputQueryParams, describe.inputQueryParams, "inputQueryParams"),
            exe.submit(task_runner, inputs.inputVariables, describe.inputVariables, "inputVariables"),
            exe.submit(task_runner, inputs.inputHeaders, describe.inputHeaders, "inputHeaders"),
            exe.submit(task_runner, inputs.inputCookies, describe.inputCookies, "inputCookies"),
        ]

        for f in as_completed(futures):
            target_attr, value = f.result()
            setattr(gen_inputs, target_attr, value)

        return gen_inputs


    def generate_pydantic_class(self, dto):
        """
        Converts DTO schema → Pydantic class dynamically using exec().
        """

        if isinstance(dto, dict):
            class_name = dto.get("name") or "GeneratedClass"
            fields = dto.get("fields") or []
            description = dto.get("description", "")
        else:
            class_name = getattr(dto, "name", None) or "GeneratedClass"
            fields = getattr(dto, "fields", None) or []
            description = getattr(dto, "description", "")

        with self._cache_lock:
            if class_name in self._class_cache:
                return None, self._class_cache[class_name]
            
            if len(self._class_cache) > 300:
                self._class_cache.clear()


        imports = [
            "from pydantic import BaseModel, Field",
            "from typing import Optional, List, Literal",
        ]
        lines = [f"class {class_name}(BaseModel):"]

        if description:
            lines.append(f'    """{description.strip()}"""')

        for f in fields:
            fname = f.name
            dtype_raw = f.dataType or "string"
            description = f.description or ""
            default_value = f.defaultValue or "null"
            options = f.options or ""

            # Type mapping
            py_type = PY_TYPE_MAP.get(dtype_raw, "str")

            if options:
                opts = [o.strip() for o in options.split(",")]
                literal_values = ", ".join([f'"{o}"' for o in opts])
                py_type = f"Literal[{literal_values}]"

            # Default
            if default_value not in ("", None, "null"):
                try:
                    default_parsed = json.loads(default_value)
                except:
                    default_parsed = default_value
                default_code = repr(default_parsed)
            else:
                default_code = "None"
                py_type = f"Optional[{py_type}]"

            field_params = []
            if description:
                field_params.append(f'description="{description}"')

            field_code = f"Field({default_code}, {', '.join(field_params)})"
            lines.append(f"    {fname}: {py_type} = {field_code}")

        class_code = "\n".join(imports) + "\n\n" + "\n".join(lines)

        # Execute class
        local_vars = {}
        exec(class_code, {"BaseModel": BaseModel, "Field": Field, "Literal": Literal, "Optional": Optional, "List": List}, local_vars)
        clazz = local_vars[class_name]

        with self._cache_lock:
            self._class_cache[class_name] = clazz

        return class_code, clazz


    def TransformToMarkDown(self, obj) -> str:

        inputs = obj.inputs
        md = []

        md.append(f"# {obj.name}")
        md.append(f"**Method:** `{obj.httpMethod}`  **Endpoint:** `{obj.endpoint}`")
        md.append(f"**Auto Execute:** `{obj.autoExecute}`")
        md.append("---")
        md.append("## ⚙️ Inputs")
        md.append(f"**Description:** {obj.description}")

        # Helper for repeating param blocks
        def add_param_block(title, data):
            if not data:
                return
            md.append(f"\n### 🔸 {title}\n")
            for k, v in data.items():
                md.append(f"{k}: {v}")

        add_param_block("Path Parameters", inputs.inputPathParams)
        add_param_block("Query Parameters", inputs.inputQueryParams)
        add_param_block("Variables", inputs.inputVariables)
        add_param_block("Headers", inputs.inputHeaders)
        add_param_block("Cookies", inputs.inputCookies)

        # Body
        if inputs.inputBody:
            json_str = list(inputs.inputBody.items())[0][1]
            pretty = json.dumps(json.loads(json_str), indent=4, ensure_ascii=False)
            md.append("\n### 🔸 Request Body\n```json")
            md.append(pretty)
            md.append("```\n---")

        # Response
        md.append("\n## 📦 Response")
        if obj.returnDescription:
            md.append(f"**Description:** {obj.returnDescription}")

        if obj.outputBody:
            pretty_out = json.dumps(json.loads(obj.outputBody), indent=4, ensure_ascii=False)
            md.append("\n### 🔸 Response Body\n```json")
            md.append(pretty_out)
            md.append("```")

        return "\n".join(md)


    def Fetch(self, q: query, FetchType: Literal["INPUT", "RETURN"]) -> Dict[str, Inputs]:
        results: Dict[str, Inputs] = {}
        if(FetchType == "RETURN"):
            objects = self.fetchApi.searchMatchesForReturnDescription(q)
        else:
            objects = self.fetchApi.searchMatchesForInputDescription(q)

        items = list(objects.inputs)
        if not items:
            return results
        
        max_workers = min(len(items), 32)
        future_to_meta = {}

        with ThreadPoolExecutor(max_workers=max_workers) as exe:
            for item in items:
                fut_inp = exe.submit(self.BuildPydanticForInputs, item)
                fut_md = exe.submit(self.TransformToMarkDown, item)
                fut_out = exe.submit(self.pydanticForBody, getattr(item, "responseBody", None), getattr(item, "dtoSchemas", {}) or {})

                future_to_meta[fut_inp] = (item, "input")
                future_to_meta[fut_md] = (item, "markdown")
                future_to_meta[fut_out] = (item, "output")

        intermediate_results = {}
        for future in as_completed(future_to_meta):
            item, meta_type = future_to_meta[future]
            name = item.name
            if name not in intermediate_results:
                intermediate_results[name] = {"item": item, "input": None, "markdown": None, "output": None}
            intermediate_results[name][meta_type] = future.result()

        for item in items:
            result = Inputs()
            returnInputs = ReturnInputs()
            result.example = returnInputs.grpcToReturnInputs(inputs=item.inputs)
            result.markDown = intermediate_results[item.name]["markdown"]
            result.input = intermediate_results[item.name]["input"]
            result.output = intermediate_results[item.name]["output"]
            result.globalPath = item.globalPath
            result.name = item.name
            result.endpoint = item.endpoint
            result.httpMethod = item.httpMethod
            result.description = item.description
            result.returnDescription = item.returnDescription
            result.autoExecute = item.autoExecute
            results[item.name] = result
        return results

# query = query(query="Attendance post and get", limit=2)
# gen = GeneratePydantic()
# output = gen.Fetch(query)
# print(output)