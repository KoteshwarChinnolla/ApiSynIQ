package com.APISynIq.ApiResolver.Controller;

import com.APISynIq.ApiResolver.Entity.EndpointDataEntity;
import com.APISynIq.ApiResolver.Service.SynIqDataService;
import com.apisyniq.grpc.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import java.util.List;

@GrpcService
public class GrpcController extends ControllerGrpc.ControllerImplBase {
    private final SynIqDataService synIqDataService;
    public GrpcController(SynIqDataService synIqDataService) {
        this.synIqDataService = synIqDataService;
    }
    @Override
    public void searchMatchesForBoth(query request,
                                     StreamObserver<InputsAndReturnsMatch> responseObserver) {
        String query = request.getQuery();
        InputsAndReturnsMatch ans = synIqDataService.queryForBoth(query, request.getLimit());
        responseObserver.onNext(ans);
        responseObserver.onCompleted();
    }

    @Override
    public void searchMatchesForInputDescription(com.apisyniq.grpc.query request,
                                                 io.grpc.stub.StreamObserver<com.apisyniq.grpc.repeatedInput> responseObserver){
        String query = request.getQuery();
        System.out.println(query);
        List<EndpointData> ans = synIqDataService.inputsDesMatch(query, request.getLimit());
        repeatedInput res = repeatedInput.newBuilder().addAllInputs(ans).build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }

    @Override
    public void searchMatchesForReturnDescription(com.apisyniq.grpc.query request,
                                                  io.grpc.stub.StreamObserver<com.apisyniq.grpc.repeatedInput> responseObserver){
        String query = request.getQuery();
        List<EndpointData> ans = synIqDataService.returnDesMatch(query, request.getLimit());
        repeatedInput res = repeatedInput.newBuilder().addAllInputs(ans).build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }

    @Override
    public void save(com.apisyniq.grpc.EndpointData request,
                     io.grpc.stub.StreamObserver<com.apisyniq.grpc.query> responseObserver){
        String data = synIqDataService.save(request).join().getId();
        responseObserver.onNext(query.newBuilder().setQuery(data).build());
        responseObserver.onCompleted();
    }

    @Override
    public void saveAll(com.apisyniq.grpc.repeatedInput request,
            io.grpc.stub.StreamObserver<com.apisyniq.grpc.query> responseObserver){
        synIqDataService.saveAll(request.getInputsList());
        responseObserver.onNext(query.newBuilder().setQuery("Done with saving").build());
        responseObserver.onCompleted();
    }

}
