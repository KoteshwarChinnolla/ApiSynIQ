import io
import wave
from Transcribe.STT.Interface import BaseSTTEngine




class WhisperSTTEngine(BaseSTTEngine):
    def __init__(self, whisper_model, sample_rate=16000):
        self.model = whisper_model
        self.sample_rate = sample_rate
        self.channels = 1
        self.sample_width = 2

    def make_wav(self, pcm_bytes):
        buf = io.BytesIO()
        with wave.open(buf, "wb") as wf:
            wf.setnchannels(self.channels)
            wf.setsampwidth(self.sample_width)
            wf.setframerate(self.sample_rate)
            wf.writeframes(pcm_bytes)
        buf.seek(0)
        return buf

    def reset(self):
        pass

    def process(self, pcm_bytes: bytes) -> str:
        wav = self.make_wav(pcm_bytes)
        if wav is None:
            return ""
        segments, _ = self.model.transcribe(wav)
        return " ".join(s.text for s in segments).strip()

    def finalize(self) -> str:
        return ""
