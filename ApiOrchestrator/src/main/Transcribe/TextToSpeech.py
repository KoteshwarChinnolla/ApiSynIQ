import threading
import queue
import sounddevice as sd
import numpy as np
from piper import PiperVoice, SynthesisConfig
from Retrieval.FetchApi import AudioStream
from Retrieval.data_pb2 import AudioChunk

class SpeakTranscribe:
    def __init__(self, max_chars: int = 150, audioChunk: AudioChunk = AudioChunk()):
        self.audioChunk = audioChunk
        self.max_chars = max_chars

        self.model_path = "./Transcribe/Utils/kathleen/model.onnx"
        if audioChunk.audio_option:
            self.model_path = f"./Transcribe/Utils/{audioChunk.audio_option}/model.onnx"

        print("[INFO] Loading Piper model...")
        self.voice = PiperVoice.load(self.model_path)
        print("[INFO] Piper loaded.")

        self.audioStream = AudioStream()
        self.text_queue = queue.Queue()
        self.running = False

        self.tts_thread = threading.Thread(target=self._tts_worker, daemon=True)

        self.syn_config = SynthesisConfig(
            volume=1.0,
            length_scale=1.0,
            noise_scale=0.45,
            noise_w_scale=0.7,
            normalize_audio=True,
        )

    def _start(self):
        if not self.running:
            self.running = True
            self.tts_thread.start()

    def _stop(self):
        self.running = False
        self.text_queue.put(None)
        self.tts_thread.join()

    def _tts_worker(self):
        print("[TTS] Worker started")

        while True:
            text = self.text_queue.get()
            if text is None:
                break

            print(f"[TTS] Synthesizing: {text}")

            audio_chunks = self.voice.synthesize(text, syn_config=self.syn_config)

            for chunk in audio_chunks:

                send_to_grpc = AudioChunk(
                    username=self.audioChunk.username,
                    session_id=self.audioChunk.session_id,
                    stream_id=self.audioChunk.stream_id,
                    language=self.audioChunk.language,
                    audio_option=self.audioChunk.audio_option,
                    text=text,
                    audio_bytes=chunk.audio_int16_bytes,
                    sample_rate=chunk.sample_rate,
                    channels=1
                )

                self.audioStream.push_chunk(send_to_grpc)

            self.audioStream.flush()

        print("[TTS] Worker stopped")

    def tts_worker(self, text: str):
        print("[INFO] TTS text received:", text)
        self._start()
        self.text_queue.put(text.strip())
        self.running=False
        self.text_queue.put(None)
        self.tts_thread.join()

        
        
# --------------------------
# Example usage
# --------------------------
# if __name__ == "__main__":
    # tts_system = PiperTTS(model_path="en_GB-semaine-medium.onnx")

#     text = """
# Go 1.22 brings two enhancements to the net/http package’s router: method matching and wildcards. 
# These features let you express common routes as patterns instead of Go code. 
# Although they are simple to explain and use, it was a challenge to come up with the right rules for selecting the winning pattern when several match a request.
# """
#     for token in text.split():
#         tts_system.speak(token)
#     tts_system.shutDown()
