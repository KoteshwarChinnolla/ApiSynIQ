from Transcribe.STT.Vosk import VoskSTTEngine
from Transcribe.STT.Whisper import WhisperSTTEngine
import queue
import threading
from Transcribe.TextToSpeech import SpeakTranscribe
from Retrieval.data_pb2 import AudioChunk
from faster_whisper import WhisperModel
from vosk import Model


whisperModel = None
voskModel=None

class InitTextModelsWhisper:
    def __init__(self):
        global whisperModel
        whisperModel = WhisperModel("tiny.en", device="cpu")

class InitTextModelsVosk:
    def __init__(self):
        global voskModel
        voskModel = Model(lang="en-us")

class STTEngineFactory:
    @staticmethod
    def create(engine_type: str):
        engine_type = engine_type.lower()

        if engine_type == "vosk":
            return VoskSTTEngine(model=voskModel)

        if engine_type == "whisper":
            return WhisperSTTEngine(whisper_model=whisperModel)

        raise ValueError(f"Unknown engine type: {engine_type}")

class STTController:
    def __init__(self, engine="vosk", sample_rate=16000, delay_seconds=3):
        self.engine = STTEngineFactory.create(engine)
        self.sample_rate = sample_rate
        self.sample_width = 2

        self.text_stream = None
        self.audio_chunk = None

        self.text_buffer = []
        self.pcm_buffer = queue.Queue()
        self.pcm_collector = bytearray()

        self.target_chunk_bytes = (
            sample_rate * self.sample_width * delay_seconds
        )

        self.worker = threading.Thread(target=self._worker, daemon=True)

    def loadAudio(self, chunk: AudioChunk):
        print("[INFO] Loading components...")
        self.audio_chunk = chunk
        self.text_stream = SpeakTranscribe(audioChunk=self.audio_chunk)
        self.start()

    def start(self):
        self.text_buffer.clear()
        self.pcm_buffer = queue.Queue()
        self.pcm_collector = bytearray()
        self.worker.start()

    def stop(self):
        print("[STT] Stop request received...")
        if self.worker and self.worker.is_alive():
            print("[STT] Thread is alive, stopping...")
            self.pcm_buffer.put(b"CLOSE_CONNECTION")
            self.pcm_buffer.join()

        self.text_stream.stop()
        self.text_stream = None

        final_text = " ".join(self.text_buffer).strip()
        self.text_buffer.clear()
        print("[STT] cleared and stopped")
        return final_text

    def feed(self, chunk):
        if not chunk:
            return ""
        self.text_stream.stopAbnormally()
        pcm_data = chunk.raw_audio.audio_bytes

        if pcm_data == b"STOP_AUDIO":
            if self.pcm_collector:
                self.pcm_buffer.put(self.pcm_collector)
                self.pcm_collector = bytearray()

            self.pcm_buffer.join()

            final_text = " ".join(self.text_buffer).strip()

            if self.text_stream:
                print("Sending final text:", final_text)
                for i in final_text.split():
                    self.text_stream.tts_worker(i)

            self.text_buffer.clear()
            return final_text

        self.pcm_collector.extend(pcm_data)

        if len(self.pcm_collector) >= self.target_chunk_bytes:
            print(f"Processing chunk size={len(self.pcm_collector)}")
            self.pcm_buffer.put(self.pcm_collector)
            self.pcm_collector = bytearray()
        return " ".join(self.text_buffer)

    def _worker(self):
        print("[STT] worker started")
        while True:
            pcm = self.pcm_buffer.get()

            if pcm == b"CLOSE_CONNECTION":
                self.pcm_buffer.task_done()
                break

            text = self.engine.process(pcm)
            if text:
                print("[STT] accepted:", text)
                self.text_buffer.append(text)

            self.pcm_buffer.task_done()
        print("[STT] worker stopped")
