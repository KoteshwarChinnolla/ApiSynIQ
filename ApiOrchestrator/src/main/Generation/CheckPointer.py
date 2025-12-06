import uuid
from typing import Dict


CHECKPOINT_STORE: Dict[str, Dict] = {}

async def save_checkpoint(runtime, checkpoint_id: str):
    CHECKPOINT_STORE[checkpoint_id] = {
        "state": dict(runtime.state),
        "graph_pointer": getattr(runtime, "graph_pointer", None),
        "agent_memory": getattr(runtime, "memory_snapshot", None),
    }

def load_checkpoint(checkpoint_id: str) -> Dict:
    return CHECKPOINT_STORE.get(checkpoint_id)

def checkpoint_exists(checkpoint_id: str) -> bool:
    return checkpoint_id in CHECKPOINT_STORE

def delete_checkpoint(checkpoint_id: str):
    if checkpoint_id in CHECKPOINT_STORE:
        del CHECKPOINT_STORE[checkpoint_id]

def update_checkpoint(runtime, checkpoint_id: str):
    if checkpoint_id in CHECKPOINT_STORE:
        CHECKPOINT_STORE[checkpoint_id]["state"] = dict(runtime.state)
    else:
      CHECKPOINT_STORE[checkpoint_id] = {
          "state": dict(runtime.state),
          "graph_pointer": getattr(runtime, "graph_pointer", None),
          "agent_memory": getattr(runtime, "memory_snapshot", None),
      }