from concurrent.futures import ThreadPoolExecutor
import grpc
import threading
from vosk import Model, KaldiRecognizer, SetLogLevel
import json
import subprocess
import base64

from Transcribe.TextToSpeech import SpeakTranscribe


class TextTranscriber:
    def __init__(self):
        SetLogLevel(0)
        self.model = Model(lang="en-us")
        self.sample_rate = 16000  # match your input!
        self.rec = KaldiRecognizer(self.model, self.sample_rate)
        self.rec.SetWords(True)
        self.rec.SetPartialWords(True)
        self.text = ""
        self.text_stream = SpeakTranscribe()

    def SpeechToText(self, chunk):        
        if(not chunk):
            return
        pcm_data = chunk.raw_audio.audio_bytes
        # print(str(pcm_data))
        if str(pcm_data) == "b'STOP_AUDIO'":
            text_stream = SpeakTranscribe()
            text_stream.tts_worker(self.text + ".", "kushal")
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
 
      