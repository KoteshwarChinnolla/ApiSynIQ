
from typing import Annotated, Any, Dict, Literal
from langchain.tools import tool, ToolRuntime
from langchain_core.messages import AIMessage, HumanMessage, ToolMessage
from pydantic import BaseModel

from Querying.RestApi import RequestApi
from .CheckPointer import update_checkpoint 
from .prompts import QUERY_TOOL_PROMPT, GET_API_TOOL_DESCRIPTION, READ_FILE_TOOL_DESCRIPTION, WRITE_FILE_PROMPT, EDIT_FILE_TOOL_DESCRIPTION, API_RESOLVER_TOOL_DESCRIPTION
from Retrieval.data_pb2 import Text,AudioChunk
from Retrieval.data_pb2 import query as q
from Retrieval.FetchApi import AudioStream
from Processing.StringToPydantic import GeneratePydantic
from deepagents.middleware.filesystem import TOOL_GENERATORS
from Retrieval.FetchApi import stream
from langgraph.types import Command
from .SubAgent import StateObject, SubAgent
from langchain.tools import InjectedToolCallId
from .testSubAgent import run_sub_agent
from langchain_core.tools import BaseTool, StructuredTool
from deepagents.backends.protocol import (
    BackendProtocol,
    EditResult,
    SandboxBackendProtocol,
    WriteResult,
)

queries = RequestApi()


class Schema(BaseModel):
    body: Any = None
    path: Any = None
    query: Any = None
    variables: Any = None
    headers: Any = None
    cookies: Any = None


@tool(description=QUERY_TOOL_PROMPT)
def query(inputs: Dict, runtime: ToolRuntime) -> str:
    """Takes in the input Parameters and returns the response from the API.
    
    Args:
        inputs(Dict) : Final json in that is verified and matches the pydantic schema.
    Returns:
        str: The response from the API.
    """
    if not inputs:
        raise ValueError("It is not possible to call the API with empty inputs.")

    
    print("Tool call received: ", inputs) 
    state = runtime.state
    query_response = queries.query(inp=inputs, method=state["method"], url=state["url"])
    state["query_response"] = query_response
    print("Result of API call: ", query_response)
    text = str(query_response)
    
    if not text:
        return ""
    config = runtime.context

    grpc_send = AudioChunk(
        text=text,
        username=config.user_name,
        session_id=config.session_id,
        stream_id=config.stream_id,
        language="en-US",
        options={
            "content": "text",
            "role": "query_tool",
            "status": "success",
        }
    )
    
    stream.push_audio_chunk(grpc_send)
    stream.flush()
    return text


@tool(description=GET_API_TOOL_DESCRIPTION)
def get_apis(search_query:str, type:Literal["INPUT", "RETURN"], count: int, runtime: ToolRuntime, tool_call_id: Annotated[str, InjectedToolCallId]) -> Command:
    """
    Retrieves relevant API specifications via vector search.

    Args:
        search_query (str): A concise semantic description of the API intent.
        type (Literal["INPUT", "RETURN"]): Whether the API is used to send or retrieve data.
        count (int): Maximum number of APIs to return.

    Returns:
        Dict: A dictionary containing API definitions.
    """
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
        }
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
    def sync_get_apis(search_query: str, type: Literal["INPUT", "RETURN"], count: int, runtime: ToolRuntime) -> Dict:
        return get_apis(search_query, type, count, runtime)

    async def async_get_apis(search_query: str, type: Literal["INPUT", "RETURN"], count: int, runtime: ToolRuntime) -> Dict:
        return get_apis(search_query, type, count, runtime)


    return StructuredTool(
        name="get_apis",
        description=tool_description,
        func=sync_get_apis,
        coroutine=async_get_apis,
    )


SELECTED_TOOL_NAMES = {
    "read_file",
    "write_file",
    "edit_file",
}


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

async def api_resolver(task, selected_api, runtime):
    config = runtime.context
    state = runtime.state
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
    return await run_sub_agent(chunk=text, state=api_resolver_state_dict)

def _create_api_resolver(tool_description: str | None = None):

    tool_description = tool_description or API_RESOLVER_TOOL_DESCRIPTION

    def api_resolver_sync(task: str, selected_api: str, runtime: ToolRuntime):
        return api_resolver(task, selected_api, runtime)
         
    
    async def async_api_resolver(task: str, selected_api: str, runtime: ToolRuntime):
        return await api_resolver(task, selected_api, runtime)    
    
    return StructuredTool(
        name="api_resolver",
        description=tool_description,
        func=api_resolver_sync,
        coroutine=async_api_resolver,
    )


DEEPAGENT_TOOLS = get_todo_filesystem_tools(
    backend="filesystem",
    custom_tool_descriptions=custom_tool_descriptions
)

DEEPAGENT_TOOLS.extend([
    get_apis_tool_generator(custom_tool_descriptions=GET_API_TOOL_DESCRIPTION),
    _create_api_resolver(custom_tool_descriptions=API_RESOLVER_TOOL_DESCRIPTION)
])
