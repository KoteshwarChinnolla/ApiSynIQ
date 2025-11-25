
from concurrent.futures import ThreadPoolExecutor

from Transcribe.SpeechToText import TextTranscriber
from Transcribe.TextToSpeech import SpeakTranscribe
from . import data_pb2_grpc as grpc_services
from .data_pb2 import IncomingAudio

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
        self.audio_stream = TextTranscriber()
        
    def UploadAudio(self, request_iterator, context):
        print("[INFO] Started Receiving...")
        for chunk in request_iterator:
            packet_type = chunk.WhichOneof("packet")
            print(packet_type)
            if packet_type=="audio_in" :
                print(chunk)
            else:
                final_text = self.audio_stream.SpeechToText(chunk)
                print(final_text)
        
