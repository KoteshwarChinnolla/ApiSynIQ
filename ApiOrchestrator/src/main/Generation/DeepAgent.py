from deepagents import FilesystemMiddleware, create_deep_agent
from langchain.agents.middleware import TodoListMiddleware

deepagent_middleware = [
        TodoListMiddleware(),
        FilesystemMiddleware()]