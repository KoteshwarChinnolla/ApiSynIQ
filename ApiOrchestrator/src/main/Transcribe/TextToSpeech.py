import threading
import queue
import sounddevice as sd
import numpy as np
from piper import PiperVoice, SynthesisConfig
from Retrieval.FetchApi import AudioStream
from Retrieval.data_pb2 import AudioChunk


voice_models = {}

class InitVoiceModels:
    def __init__(self):
        global voice_models
        voices = ["kathleen", "kushal", "semaine"]
        for voice in voices:
            voice_models[voice] = PiperVoice.load(f"./Transcribe/Utils/{voice}/model.onnx")

class SpeakTranscribe:
    def __init__(self, max_chars: int = 150, audioChunk: AudioChunk = None):
        self.audioChunk = audioChunk
        self.max_chars = max_chars 

        self.voice = voice_models[audioChunk.audio_option]

        self.current_text = ""
        self.audioStream = AudioStream()
        self.text_queue = queue.Queue()

        self.tts_worker_thread = threading.Thread(target=self._tts_worker, daemon=True)
        self.tts_worker_thread.start()
        self.syn_config = SynthesisConfig(
            volume=1.0,
            length_scale=1.0,
            noise_scale=0.45,
            noise_w_scale=0.7,
            normalize_audio=True,
        )

    def stop(self):
        print("[TTS] Stop request received...")
        if self.tts_worker_thread and self.tts_worker_thread.is_alive():
            print("[TTS] Thread is alive, stopping...")

            if self.current_text:
                print(f"[TTS] Flushing remaining text: {self.current_text}")
                self.text_queue.put(self.current_text)

            self.text_queue.put(None)
            self.text_queue.join()

        self.current_text = ""
        self.clear_queue(self.text_queue)

        print("[TTS] Cleared and stopped")

    def clear_queue(self, q: queue.Queue):
        while not q.empty():
            try:
                q.get_nowait()
                q.task_done()
            except queue.Empty:
                break

    
    def stopAbnormally(self):
        self.current_text = ""
        self.clear_queue(self.text_queue)


    def _tts_worker(self):
        print("[TTS] worker started")

        while True:
            text = self.text_queue.get()

            if text is None:
                self.text_queue.task_done()
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

                self.audioStream.push_audio_chunk(send_to_grpc)

            self.audioStream.flush()
            self.text_queue.task_done()

        print("[TTS] Worker stopped")

    def tts_worker(self, text: str):
        """Collects text and sends complete sentences to queue."""

        punctuation = {".", "!", "?"}

        if text[-1] in punctuation:
            sentence = (self.current_text + text).strip()
            print(sentence)
            self.text_queue.put(sentence)
            
            self.current_text = ""
        else:
            self.current_text += (" " + text if self.current_text else text)
            # print(self.current_text)



        
        
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
