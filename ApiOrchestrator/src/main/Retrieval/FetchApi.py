import grpc
from . import data_pb2_grpc as grpc_services
from .data_pb2 import query, InputsAndReturnsMatch, repeatedInput

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
    
# request = query(query=input("Enter the query "), limit=int(input("Enter the limit ")))
# request = query(query="Fetches a calendar record for a specific employee on a given date.", limit=2)
# print(f"1->both \n2->input \n3->return")
# match str(2):
#   case "1":
#     print(FetchApi().searchMatchesForBoth(request))
#   case "2":
#     print(FetchApi().searchMatchesForInputDescription(request))
#   case "3":
#     print(FetchApi().searchMatchesForReturnDescription(request))

# i want to apply for a leave please elp me with that
# can i get the calender details for this month