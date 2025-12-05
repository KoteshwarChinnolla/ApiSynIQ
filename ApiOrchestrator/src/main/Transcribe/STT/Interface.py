class BaseSTTEngine:
    def reset(self):
        raise NotImplementedError

    def process(self, pcm_bytes: bytes) -> str:
        raise NotImplementedError

    def finalize(self) -> str:
        raise NotImplementedError
    