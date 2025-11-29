
from concurrent.futures import ThreadPoolExecutor

from Transcribe.SpeechToText import TextTranscriberVosk
from . import data_pb2_grpc as grpc_services
from .data_pb2 import IncomingAudio, AudioChunk, RawAudio
import grpc


class GrpcServer:
  def __init__(self):
     self.serve()

  def serve(self):
      server = grpc.server(thread_pool=ThreadPoolExecutor(max_workers=10))
      grpc_services.add_TTSServiceServicer_to_server(
          TTSServiceServicer(), server
      )
      server.add_insecure_port("[::]:7324")
      server.start()
      print("Server started on port 7324")
      server.wait_for_termination()


class TTSServiceServicer(grpc_services.TTSServiceServicer):

    def __init__(self):
        # key = session_id, value = TextTranscriber instance
        self.sessions = {}

    def get_session(self, session_id):
        if session_id not in self.sessions:
            print(f"[INFO] Creating new STT session for {session_id}")
            self.sessions[session_id] = TextTranscriberVosk()
        return self.sessions[session_id]
    
    def remove_session(self, session_id):
        if session_id in self.sessions:
            del self.sessions[session_id]
            print(f"[INFO] Session {session_id} removed")
        else:
            print(f"[INFO] Session {session_id} not found")
        return

    def UploadAudio(self, request_iterator, context):
        print("[INFO] Started Receiving...")

        current_session_id = None
        stt_session = None

        for chunk in request_iterator:
            packet_type = chunk.WhichOneof("packet")

            if packet_type == "audio_in":
                audio = chunk.audio_in
                current_session_id = audio.session_id

                stt_session = self.get_session(audio.session_id)

                audioChunk = AudioChunk(
                    username=audio.username,
                    session_id=audio.session_id,
                    stream_id=audio.stream_id,
                    language=audio.language,
                    audio_option=audio.audio_option
                )

                stt_session.LoadAudio(audioChunk)

            elif packet_type == "error":
                print(f"[{current_session_id}] -> {chunk.error.error}")
                stt_session.stop()
                self.remove_session(current_session_id)

            else:
                if current_session_id is None:
                    continue
                stt_session = self.get_session(current_session_id)
                final_text = stt_session.SpeechToTextVosk(chunk)

                # print(f"[{current_session_id}] -> {final_text}")