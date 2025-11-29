from concurrent.futures import ThreadPoolExecutor
import queue
import grpc
import threading
from vosk import Model, KaldiRecognizer, SetLogLevel
import json
import subprocess
import base64
import numpy as np
import sounddevice as sd
import wave
import io

from Transcribe.TextToSpeech import SpeakTranscribe, voice_models
from Retrieval.data_pb2 import AudioChunk
from faster_whisper import WhisperModel

vosk = None

class InitTextModels:
    def __init__(self):
        global vosk
        vosk = Model(lang="en-us")

class TextTranscriberVosk:
    def __init__(self, sample_rate: int = 16000):
        SetLogLevel(0)
        self.model = vosk
        self.sample_rate = sample_rate
        self.sample_width = 2  # 16-bit PCM

        self.rec = KaldiRecognizer(self.model, self.sample_rate)
        self.rec.SetWords(True)
        self.rec.SetPartialWords(True)

        self.audio_chunk: AudioChunk
        self.text_stream: SpeakTranscribe

        self.text_buffer = []
        self.pcm_buffer = queue.Queue()
        self.pcm_collector = bytearray()

        self.delay_seconds = 3

        self.target_chunk_bytes = (
            self.sample_rate * self.sample_width * self.delay_seconds
        )

        self.stt_worker_thread = threading.Thread(target=self.Vosk_worker, daemon=True)



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
        self.rec.Reset()
        print("[STT] cleared and stopped")
        return final_text


    def LoadAudio(self, chunk: AudioChunk):
        self.audio_chunk = chunk
        self.text_stream = SpeakTranscribe(audioChunk=self.audio_chunk)
        # self.text_stream.start()
        self.start()

    def Vosk_worker(self):
        print("[STT] Worker started")
        while True:
            pcm_data = self.pcm_buffer.get()
            if pcm_data == b"CLOSE_CONNECTION":
                self.pcm_buffer.task_done()
                break

            if isinstance(pcm_data, bytearray):
                pcm_data = bytes(pcm_data) 


            accepted = self.rec.AcceptWaveform(pcm_data)
            print("Accepted?", accepted)

            if accepted:
                result = json.loads(self.rec.Result())
                if "text" in result and result["text"]:
                    self.text_buffer.append(result["text"])

            self.pcm_buffer.task_done()

        print("[STT] Worker stopped")

    def SpeechToTextVosk(self, chunk):
        if not chunk:
            return ""
        
        
        pcm_data = chunk.raw_audio.audio_bytes

        # ---------- STOP AUDIO ----------
        if pcm_data == b"STOP_AUDIO":

            if self.pcm_collector:
                self.pcm_buffer.put(self.pcm_collector)
                self.pcm_collector = bytearray()

            self.pcm_buffer.join()

            final_text = " ".join(self.text_buffer).strip()
            
            if self.text_stream:
                print("Sending final text:", final_text)
                self.text_stream.start()
                for i in final_text.split():
                    self.text_stream.tts_worker(i)
                self.text_stream.stop()

            self.text_buffer.clear()
            self.rec.Reset()

            return final_text

        self.pcm_collector.extend(pcm_data)

        if len(self.pcm_collector) >= self.target_chunk_bytes:
            print(f"Processing chunk size={len(self.pcm_collector)}")
            self.pcm_buffer.put(self.pcm_collector)
            self.pcm_collector = bytearray()

        return " ".join(self.text_buffer)

 

class TextTranscriber:
    def __init__(self, sample_rate: int = 16000):
        SetLogLevel(0)
        self.model = Model(lang="en-us")
        self.whisper_model = WhisperModel("small", device="cpu")

        self.sample_rate = sample_rate
        self.channels = 1
        self.sample_width = 2

        self.chunk_window_ms = 3000            # 3 seconds
        self.bytes_per_second = sample_rate * self.sample_width
        self.target_chunk_bytes = self.bytes_per_second * 3

        self.pcm_collector = bytearray()
        self.pcm_buffer = queue.Queue()
        self.text_buffer = []
        self.audio_chunk: None
        self.text_stream: SpeakTranscribe

        # Start background whisper worker
        threading.Thread(target=self.whisper_worker, daemon=True).start()

    def LoadAudio(self, chunk: AudioChunk):
        self.audio_chunk = chunk
        self.text_stream = SpeakTranscribe(audioChunk=self.audio_chunk)

    def make_wav_bytes(self, pcm_bytes: bytes) -> io.BytesIO:
        """Convert PCM -> WAV memory file."""
        wav_io = io.BytesIO()
        with wave.open(wav_io, "wb") as wf:
            wf.setnchannels(self.channels)
            wf.setsampwidth(self.sample_width)
            wf.setframerate(self.sample_rate)
            wf.writeframes(pcm_bytes)
        wav_io.seek(0)
        return wav_io

    def whisper_worker(self):
        """Background thread that processes WAV chunks."""
        while True:
            wav_io = self.pcm_buffer.get()

            segments, _ = self.whisper_model.transcribe(wav_io)
            text = " ".join(seg.text for seg in segments).strip()

            if text:
                self.text_buffer.append(text)
                print(text)

            self.pcm_buffer.task_done()

    def SpeechToTextWhisper(self, chunk):
        if not chunk:
            return ""

        pcm_data = chunk.raw_audio.audio_bytes

        if pcm_data == b"STOP_AUDIO":

            if self.pcm_collector:
                wav_io = self.make_wav_bytes(self.pcm_collector)
                self.pcm_buffer.put(wav_io)
                self.pcm_collector = bytearray()

            self.pcm_buffer.join()
            
            return " ".join(self.text_buffer).strip()

        self.pcm_collector.extend(pcm_data)

        if len(self.pcm_collector) >= self.target_chunk_bytes:
            print("Processing chunk" + str(len(self.pcm_collector)))
            wav_io = self.make_wav_bytes(self.pcm_collector)
            self.pcm_buffer.put(wav_io)
            self.pcm_collector = bytearray()

        return " ".join(self.text_buffer)
