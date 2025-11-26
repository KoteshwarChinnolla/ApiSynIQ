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
        self.sample_rate = sample_rate  # match your input!
        self.AudioChunk = None
        self.rec = KaldiRecognizer(self.model, self.sample_rate)
        self.rec.SetWords(True)
        self.rec.SetPartialWords(True)
        self.text = ""
        self.text_stream = None

    def LoadAudio(self, chunk: AudioChunk):
        print("At LoadAudio")
        print(chunk)
        self.AudioChunk = chunk
        self.text_stream = SpeakTranscribe(audioChunk=self.AudioChunk)


    def SpeechToText(self, chunk):        
        if(not chunk):
            return
        pcm_data = chunk.raw_audio.audio_bytes
        if str(pcm_data) == "b'STOP_AUDIO'":
            self.text_stream.tts_worker(self.text + ".", )
            self.text = ""
            return self.text
        
        
        ok = self.rec.AcceptWaveform(pcm_data)
        print("Accepted?", ok)
        if ok:
            result = json.loads(self.rec.Result())
            if "text" in result:
                self.text += " " + result["text"]
        else:
            partial = json.loads(self.rec.PartialResult())

        return self.text
 
      