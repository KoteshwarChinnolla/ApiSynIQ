import io
import wave
import queue
import threading
# import numpy as np
# import sounddevice as sd
from faster_whisper import WhisperModel
from Retrieval.data_pb2 import AudioChunk
from concurrent.futures import ThreadPoolExecutor
from Transcribe.TextToSpeech import SpeakTranscribe, voice_models

whisper = None


class InitTextModelsWhisper:
    def __init__(self):
        global whisper
        whisper = WhisperModel("tiny.en", device="cpu")


class TextTranscriberWhisper:
    def __init__(self, sample_rate: int = 16000):
        self.sample_rate = sample_rate
        self.sample_width = 2
        self.channels = 1

        self.audio_chunk: AudioChunk
        self.text_stream: SpeakTranscribe

        self.text_buffer = []
        self.pcm_buffer = queue.Queue()
        self.pcm_collector = bytearray()

        self.delay_seconds = 5
        self.target_chunk_bytes = (
            self.sample_rate * self.sample_width * self.delay_seconds
        )
        self.whisper_model = whisper

        # Start background worker
        self.stt_worker_thread = threading.Thread(
            target=self.Whisper_worker, daemon=True
        )

    def start(self):
        self.text_buffer = []
        self.pcm_buffer = queue.Queue()
        self.pcm_collector = bytearray()
        self.stt_worker_thread.start()
        


    def stop(self):
        if self.stt_worker_thread and self.stt_worker_thread.is_alive():
            self.pcm_buffer.put(b"CLOSE_CONNECTION")
            self.pcm_buffer.join()

        self.text_stream.stop()
        self.text_stream = None

        final_text = " ".join(self.text_buffer).strip()
        self.text_buffer.clear()
        print("[STT] cleared and stopped")
        return final_text

    def LoadAudio(self, chunk: AudioChunk):
        self.audio_chunk = chunk
        self.text_stream = SpeakTranscribe(audioChunk=self.audio_chunk)
        self.text_stream.start()
        self.start()

    def make_wav_bytes(self, pcm_bytes: bytes) -> io.BytesIO:
        wav_io = io.BytesIO()
        with wave.open(wav_io, "wb") as wf:
            wf.setnchannels(self.channels)
            wf.setsampwidth(self.sample_width)
            wf.setframerate(self.sample_rate)
            wf.writeframes(pcm_bytes)
        wav_io.seek(0)
        return wav_io

    def Whisper_worker(self):
        print("[STT] Thread Started")
        while True:
            pcm_bytes = self.pcm_buffer.get()
            if wav_io == b"CLOSE_CONNECTION":
                self.pcm_buffer.task_done()
                break
            wav_io = self.make_wav_bytes(pcm_bytes)
            segments, _ = self.whisper_model.transcribe(wav_io)
            text = " ".join(seg.text for seg in segments).strip()

            if text:
                self.text_buffer.append(text)
                print("Accepted:", text)

            self.pcm_buffer.task_done()

        print("[STT] Worker stopped")

    def SpeechToTextWhisper(self, chunk):
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
                # self.text_stream.start()
                for i in final_text.split():
                    self.text_stream.tts_worker(i)
                # self.text_stream.stop()

            self.text_buffer.clear()
            return final_text

        self.pcm_collector.extend(pcm_data)

        if len(self.pcm_collector) >= self.target_chunk_bytes:
            print(f"Processing chunk size={len(self.pcm_collector)}")
            self.pcm_buffer.put(self.pcm_collector)
            self.pcm_collector = bytearray()
        return " ".join(self.text_buffer)


