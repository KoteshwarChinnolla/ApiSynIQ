from Transcribe.TextToSpeech import InitVoiceModels
from Transcribe.SpeechToText import InitTextModels
from Retrieval.GrpcServer import GrpcServer



def main():
  InitVoiceModels()
  InitTextModels()
  GrpcServer()