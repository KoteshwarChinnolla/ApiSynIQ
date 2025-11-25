import threading
import queue
import sounddevice as sd
import numpy as np
from piper import PiperVoice, SynthesisConfig
from Retrieval.FetchApi import AudioStream
from Retrieval.data_pb2 import AudioChunk

class SpeakTranscribe:
    def __init__(self, max_chars: int = 150):
        self.model_path = "./Transcribe/Utils/kushal/model.onnx"
        self.max_chars = max_chars
        self.audioStream = AudioStream()
        # Queues
        self.text_queue = queue.Queue()
        self.audio_queue = queue.Queue()

        self.shutdown_flag = False

        # Chunk buffer for accumulating tokens
        self.chunk = ""

        print("[INFO] Loading Piper model...")
        self.voice = PiperVoice.load(self.model_path)

        self.syn_config = SynthesisConfig(
            volume=1.0,
            length_scale=1.0,
            noise_scale=0.45,
            noise_w_scale=0.7,
            normalize_audio=True,
        )

        # Threads
        self.tts_thread = threading.Thread(target=self._tts_worker, daemon=True)
        self.audio_thread = threading.Thread(target=self._audio_worker, daemon=True)

    def _start(self):
        print("[INFO] Starting TTS system...")
        self.shutdown_flag = False
        self.tts_thread.start()
        self.audio_thread.start()

    def _stop(self):
        print("[INFO] Shutting down TTS system...")
        self.shutdown_flag = True
        self.text_queue.put(None)
        self.audio_queue.put(None)
        self.tts_thread.join()
        self.audio_thread.join()
        print("[INFO] TTS system stopped.")

    def _tts_worker(self):
        print("[TTS] Worker started")
        
        while not self.shutdown_flag:
            try:
                text = self.text_queue.get(timeout=0.2)
            except queue.Empty:
                continue

            if text is None:
                break

            print(f"[TTS] Generating audio for: \"{text}\"")
            audio_chunks = self.voice.synthesize(text, syn_config=self.syn_config)

            for chunk in audio_chunks:
                float_audio = chunk.audio_int16_bytes  
                send_to_grpc = AudioChunk(audio_bytes=float_audio, sample_rate=chunk.sample_rate, channels=1, text=text, username="username", session_id="session_id", stream_id="stream_id", language="language", audio_option="audio_option")
                self.audioStream.push_chunk(send_to_grpc)
                
                # self.audio_queue.put((float_audio, chunk.sample_rate, text))
                # Sending to frontend logic
            self.audioStream.flush()
            self.text_queue.task_done()

        print("[TTS] Worker shutting down")


    # Optional
    def _audio_worker(self):
        print("[AUDIO] Worker started")
        while True:
            item = self.audio_queue.get()
            if item is None:
                break

            samples, sr, original_text = item
            print(f"[AUDIO] Playing chunk: \"{original_text}\"")
            sd.play(samples, sr)
            sd.wait()
            self.audio_queue.task_done()

        print("[AUDIO] Worker shutting down")

    def _speak(self, token: str):
        """Accumulate tokens and send completed sentences to TTS."""
        self.chunk += token + " "
        if token.endswith((".", "!", "?")):
            self.text_queue.put(self.chunk.strip())
            self.chunk = ""

    def shutDown(self):
        self.text_queue.join()
        self.audio_queue.join()
        self.audioStream.flush()
        self._stop()

    def tts_worker(self ,text:str, voice:str = "kushal"):
        print("[INFO] Received text:", text)
        self._start()
        try:
            self.model_path = "./Transcribe/Utils/"+voice+"/model.onnx"
            for token in text.split():
                self._speak(token)
            
        except Exception as e:
            print("[ERROR] Error occurred:", e)

        self.shutDown()

        
        
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
