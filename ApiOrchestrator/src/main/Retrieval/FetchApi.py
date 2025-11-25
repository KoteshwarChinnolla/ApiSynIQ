from concurrent.futures import ThreadPoolExecutor
import grpc
from . import data_pb2_grpc as grpc_services
from .data_pb2 import query, InputsAndReturnsMatch, repeatedInput, AudioChunk, Empty, StreamPacket



class FetchApi:
  def __init__(self):
    self.channel = grpc.insecure_channel('localhost:7322')
    self.stub = grpc_services.ControllerStub(self.channel)

  def searchMatchesForBoth(self, query: query) -> InputsAndReturnsMatch:
    return self.stub.searchMatchesForBoth(query)
  
  def searchMatchesForInputDescription(self, query: query) -> repeatedInput:
    return self.stub.searchMatchesForInputDescription(query)
  
  def searchMatchesForReturnDescription(self, query: query) -> repeatedInput:
    return self.stub.searchMatchesForReturnDescription(query)

class AudioStream:
    def __init__(self):
        channel = grpc.insecure_channel("localhost:7323")
        stub = grpc_services.TTSServiceStub(channel)
        self.stub = stub
        self._queue = [] 

    def push_chunk(self, audio_chunk):
        pocket = StreamPacket(audio_out=audio_chunk)
        self._queue.append(pocket)

    def flush(self):
        # UploadAudio expects an iterator of AudioChunk
        def gen():
            while self._queue:
                yield self._queue.pop(0)

        self.stub.UploadAudio(gen())