
from typing import Annotated, Any, Dict, List, Literal
from langchain.tools import tool, ToolRuntime
from langchain_core.messages import AIMessage, HumanMessage, ToolMessage
from pydantic import BaseModel, Field

from .CheckPointer import update_checkpoint 
from .prompts import QUERY_TOOL_PROMPT, GET_API_TOOL_DESCRIPTION, READ_FILE_TOOL_DESCRIPTION, WRITE_FILE_PROMPT, EDIT_FILE_TOOL_DESCRIPTION, API_RESOLVER_TOOL_DESCRIPTION
from Retrieval.data_pb2 import Text,AudioChunk
from Retrieval.data_pb2 import query as q
from Processing.StringToPydantic import GeneratePydantic
from deepagents.middleware.filesystem import TOOL_GENERATORS
from langgraph.types import Command
from langchain.tools import InjectedToolCallId
from langchain_core.tools import BaseTool, StructuredTool
from deepagents.backends.protocol import (
    BackendProtocol,
    EditResult,
    SandboxBackendProtocol,
    WriteResult,
)
from .Utils import run_sub_agent
from .Data import Plan

class Schema(BaseModel):
    body: Any = None
    path: Any = None
    query: Any = None
    variables: Any = None
    headers: Any = None
    cookies: Any = None



class GetApisInput(BaseModel):
    search_query: str = Field(..., description="Semantic description of the API intent")
    type: str = Field(..., description="INPUT or RETURN")
    count: int = Field(..., ge=1, description="Number of APIs to retrieve")

class ApiResolverInput(BaseModel):
    task: str = Field(..., description="Task to perform using the selected API")
    selected_api: str = Field(..., description="API name selected from get_apis")

class FileInput(BaseModel):
    path: str = Field(..., description="Path to the file, must be an absolute path in the format of /todos/<session_id>.csv")
    content: str = Field(..., description="Content of the file, must be comma-separated in following format: API_NAME, EXECUTION_REASON, EXECUTION_STATUS, API_RESPONSE")

def get_apis(search_query:str, type: str, count: int, runtime: ToolRuntime, tool_call_id: Annotated[str, InjectedToolCallId]) -> Command[Literal["orchestrator"]]:

    if type not in ["INPUT", "RETURN"]:
        raise ValueError("type must be either 'INPUT' or 'RETURN'")

    gen=GeneratePydantic()

    a = q(query=search_query, limit=count)
    fetched_results=gen.Fetch(a,type)

    result =  schema_formation(fetched_results)
    state = runtime.state
    state["apis"] = result
    markdowns = {key: getattr(value, "model_card", "") for key, value in fetched_results.items()}
    return Command(
        update={
            "apis":result,
            "messages": [
                ToolMessage(f"{markdowns} Here are the API's for search query: {search_query} of type: {type}", tool_call_id=tool_call_id)
            ],
            
        },
        goto="orchestrator",
    )

def schema_formation(fetched_results):
    results = {}
    for key, selected_inputs in fetched_results.items():
        selected_api = key
        selected_markdown = getattr(selected_inputs, "markDown", "")

        body = getattr(selected_inputs.input, "inputBody", None)
        path = getattr(selected_inputs.input, "inputPathParams", None)
        query = getattr(selected_inputs.input, "inputQueryParams", None)
        variables = getattr(selected_inputs.input, "inputVariables", None)
        headers = getattr(selected_inputs.input, "inputHeaders", None)
        cookies = getattr(selected_inputs.input, "inputCookies", None)

        schema = Schema(
            body=body.model_json_schema() if body else None,
            path=path.model_json_schema() if path else None,
            query=query.model_json_schema() if query else None,
            variables=variables.model_json_schema() if variables else None,
            headers=headers.model_json_schema() if headers else None,
            cookies=cookies.model_json_schema() if cookies else None
        ).__dict__

        method = (selected_inputs.httpMethod or "GET").upper()
        base_url = selected_inputs.globalPath.rstrip("/")
        endpoint = selected_inputs.endpoint.lstrip("/")
        url = f"{base_url}/{endpoint}"

        results[key] = {
            "method": method,
            "url": url,
            "name": selected_api,
            "model_card":selected_markdown,
            "schema": schema
        }
    return results

def get_apis_tool_generator(
    custom_tool_descriptions: str | None = None,
) -> BaseTool:
    
    tool_description = custom_tool_descriptions or GET_API_TOOL_DESCRIPTION
    def sync_get_apis(search_query: str, type: str, count: int, runtime: ToolRuntime, tool_call_id: Annotated[str, InjectedToolCallId]) -> Command:
        return get_apis(search_query, type, count, runtime, tool_call_id)

    async def async_get_apis(search_query: str, type: str, count: int, runtime: ToolRuntime, tool_call_id: Annotated[str, InjectedToolCallId]) -> Command:
        return get_apis(search_query, type, count, runtime, tool_call_id)


    return StructuredTool(
        name="get_apis",
        description=tool_description,
        args_schema=GetApisInput,
        func=sync_get_apis,
        coroutine=async_get_apis,
    )


SELECTED_TOOL_NAMES = {
    # "read_file",
    "write_file",
    "edit_file",
}


def write_plan(
        file_path: str,
        content: str,
        runtime: ToolRuntime,):
    state=runtime.state
    with open(file_path, "w") as f:
        f.write(content)
        
    return Command(
        update={
            "plan": content,
            "messages": [
                ToolMessage(f"Plan saved to {file_path}", tool_call_id=runtime.tool_call_id)
            ],
        }
    )

async def edit_plan(
        file_path: str,
        content: str,
        runtime: ToolRuntime,):
    with open(file_path, "w") as f:
        f.write(content)
    return Command(
        update={
            "plan": content,
            "messages": [
                ToolMessage(f"Plan saved to {file_path}", tool_call_id=runtime.tool_call_id)
            ],
        }
    )

def _get_write_plan_tools(
    custom_tool_descriptions: dict[str, str] | None = None,
):
    tool_description = custom_tool_descriptions["write_file"] or WRITE_FILE_PROMPT

    def sync_write_plan(file_path: str, content: str, runtime: ToolRuntime, tool_call_id: Annotated[str, InjectedToolCallId]) -> Command:
        return write_plan(file_path, content, runtime)

    async def async_write_plan(file_path: str, content: str, runtime: ToolRuntime, tool_call_id: Annotated[str, InjectedToolCallId]) -> Command:
        return await write_plan(file_path, content, runtime)

    return StructuredTool(
        name="write_plan",
        description=tool_description,
        args_schema=FileInput,
        func=sync_write_plan,
        coroutine=async_write_plan,
    )

def _get_edit_plan_tools(
    custom_tool_descriptions: dict[str, str] | None = None,
):
    tool_description = custom_tool_descriptions["edit_file"] or EDIT_FILE_TOOL_DESCRIPTION

    def sync_edit_plan(file_path: str, content: str, runtime: ToolRuntime, tool_call_id: Annotated[str, InjectedToolCallId]) -> Command:
        return edit_plan(file_path, content, runtime)

    async def async_edit_plan(file_path: str, content: str, runtime: ToolRuntime, tool_call_id: Annotated[str, InjectedToolCallId]) -> Command:
        return await edit_plan(file_path, content, runtime)

    return StructuredTool(
        name="edit_plan",
        description=tool_description,
        args_schema=FileInput,
        func=sync_edit_plan,
        coroutine=async_edit_plan,
    )


def get_todo_filesystem_tools(
    backend: BackendProtocol,
    custom_tool_descriptions: dict[str, str] | None = None,
) -> list[BaseTool]:
    if custom_tool_descriptions is None:
        custom_tool_descriptions = {}

    tools = []
    for tool_name in SELECTED_TOOL_NAMES:
        tool_generator = TOOL_GENERATORS[tool_name]
        tool = tool_generator(
            backend,
            custom_tool_descriptions.get(tool_name),
        )
        tools.append(tool)

    return tools


custom_tool_descriptions = {
    "read_file": READ_FILE_TOOL_DESCRIPTION,
    "write_file": WRITE_FILE_PROMPT,
    "edit_file": EDIT_FILE_TOOL_DESCRIPTION,
}

async def api_resolver(task, selected_api, state, config):
    if "apis" not in state or selected_api not in state["apis"]:
        raise ValueError(f"API '{selected_api}' not found. Call get_apis first.")
    inputs_to_process = state["apis"][selected_api]
    SystemMessage = f"""
        Here are the schema and model card for the API to be filled:
        Schema: {inputs_to_process["schema"]}
        Model Card: {inputs_to_process["model_card"]}
    """
    api_resolver_state_dict = {"messages": [ {"role": "system","content": SystemMessage}],
                                "markdown": inputs_to_process["model_card"], 
                                "schema": inputs_to_process["schema"], 
                                "method": inputs_to_process["method"], 
                                "url": inputs_to_process["url"]}
    text = Text(
        text=task,
        username=config.user_name,
        session_id=config.session_id,
        stream_id=config.stream_id,
        language="en-US",
    )

    return run_sub_agent(text, api_resolver_state_dict)

def _create_api_resolver(tool_description: str | None = None):

    tool_description = tool_description or API_RESOLVER_TOOL_DESCRIPTION

    def api_resolver_sync(task: str, selected_api: str, runtime: ToolRuntime):
        state = runtime.state
        config = runtime.context  
        return api_resolver(task, selected_api, state, config)
         
    
    async def async_api_resolver(task: str, selected_api: str, runtime: ToolRuntime):
        state = runtime.state
        config = runtime.context 
        return await api_resolver(task, selected_api, state, config)    
    
    return StructuredTool(
        name="api_resolver",
        description=tool_description,
        args_schema=ApiResolverInput,
        func=api_resolver_sync,
        coroutine=async_api_resolver,
    )


DEEPAGENT_TOOLS = get_todo_filesystem_tools(
    backend="filesystem",
    custom_tool_descriptions=custom_tool_descriptions
)

FILE_SYSTEM_TOOLS = [
    _get_write_plan_tools(custom_tool_descriptions=custom_tool_descriptions),
    _get_edit_plan_tools(custom_tool_descriptions=custom_tool_descriptions),
]

# DEEPAGENT_TOOLS.extend([
#     get_apis_tool_generator(custom_tool_descriptions=GET_API_TOOL_DESCRIPTION),
#     _create_api_resolver(tool_description=API_RESOLVER_TOOL_DESCRIPTION)
# ])

GET_API_TOOL = [get_apis_tool_generator(custom_tool_descriptions=GET_API_TOOL_DESCRIPTION)]
