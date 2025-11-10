package com.APISynIq.ApiResolver.Controller;

import com.APISynIq.ApiResolver.Entity.SynIqData;
import com.APISynIq.ApiResolver.Service.SynIqDataService;
import com.apisyniq.grpc.*;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrpcService extends ControllerGrpc.ControllerImplBase {
    private final SynIqDataService synIqDataService;
    public GrpcService(SynIqDataService synIqDataService) {
        this.synIqDataService = synIqDataService;
    }
    @Override
    public void searchMatchesForBoth(query request,
                                     StreamObserver<InputsAndReturnsMatch> responseObserver) {
        InputsAndReturnsMatch ans = synIqDataService.queryForBoth(query.newBuilder().getQuery());
        responseObserver.onNext(ans);
        responseObserver.onCompleted();
    }

    @Override
    public void searchMatchesForInputDescription(com.apisyniq.grpc.query request,
                                                 io.grpc.stub.StreamObserver<com.apisyniq.grpc.repeatedInput> responseObserver){
        List<SynIqData> ans = synIqDataService.inputsDesMatch(query.newBuilder().getQuery());
        repeatedInput res = repeatedInput.newBuilder().addAllInputs(ans.stream().map(SynIqData::toGrpcInputData).toList()).build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }

    @Override
    public void searchMatchesForReturnDescription(com.apisyniq.grpc.query request,
                                                  io.grpc.stub.StreamObserver<com.apisyniq.grpc.repeatedInput> responseObserver){
        List<SynIqData> ans = synIqDataService.inputsDesMatch(query.newBuilder().getQuery());
        repeatedInput res = repeatedInput.newBuilder().addAllInputs(ans.stream().map(SynIqData::toGrpcInputData).toList()).build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }

    @Override
    public void save(com.apisyniq.grpc.InputData request,
                     io.grpc.stub.StreamObserver<com.apisyniq.grpc.query> responseObserver){
        String data = synIqDataService.save(request).join().getId();
        responseObserver.onNext(query.newBuilder().setQuery(data).build());
        responseObserver.onCompleted();
    }

}
