from vosk import Model, KaldiRecognizer, SetLogLevel
import json
from Transcribe.STT.Interface import BaseSTTEngine

class VoskSTTEngine(BaseSTTEngine):
    def __init__(self, model):
        SetLogLevel(0)
        self.rec = KaldiRecognizer(model, 16000)
        self.rec.SetWords(True)
        self.rec.SetPartialWords(True)

    def reset(self):
        self.rec.Reset()

    def process(self, pcm_bytes: bytes) -> str:
        if self.rec.AcceptWaveform(pcm_bytes):
            result = json.loads(self.rec.Result())
            return result.get("text", "")
        return ""

    def finalize(self) -> str:
        result = json.loads(self.rec.FinalResult())
        return result.get("text", "")