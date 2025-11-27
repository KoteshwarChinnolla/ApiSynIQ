from concurrent.futures import ThreadPoolExecutor
import grpc
import threading
from vosk import Model, KaldiRecognizer, SetLogLevel
import json
import subprocess
import base64

from Transcribe.TextToSpeech import SpeakTranscribe
from Retrieval.data_pb2 import AudioChunk


class TextTranscriber:
    def __init__(self, sample_rate: int = 16000):
        SetLogLevel(0)
        self.model = Model(lang="en-us")
        self.sample_rate = sample_rate
        self.rec = KaldiRecognizer(self.model, self.sample_rate)
        self.rec.SetWords(True)
        self.rec.SetPartialWords(True)

        self.audio_chunk: AudioChunk | None = None
        self.text_stream: SpeakTranscribe | None = None
        self.text_buffer = []

    def LoadAudio(self, chunk: AudioChunk):
        print("At LoadAudio", chunk)

        # Store metadata
        self.audio_chunk =chunk
        self.text_stream =SpeakTranscribe(audioChunk=self.audio_chunk)

    def SpeechToText(self, chunk):
        if not chunk:
            return ""

        pcm_data = chunk.raw_audio.audio_bytes

        if pcm_data == b"STOP_AUDIO":
            final_text = " ".join(self.text_buffer).strip()

            if self.text_stream:
                self.text_stream.tts_worker(final_text + ".")

            self.text_buffer.clear()

            return final_text

        accepted = self.rec.AcceptWaveform(pcm_data)
        print("Accepted?", accepted)

        if accepted:
            result = json.loads(self.rec.Result())
            if "text" in result and result["text"]:
                self.text_buffer.append(result["text"])
        else:
            _ = json.loads(self.rec.PartialResult())

        return " ".join(self.text_buffer)
 