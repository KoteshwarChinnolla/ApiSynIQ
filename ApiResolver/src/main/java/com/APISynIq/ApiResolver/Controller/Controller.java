package com.APISynIq.ApiResolver.Controller;

import com.apisyniq.grpc.EndpointData;
import com.apisyniq.grpc.InputsAndReturnsMatch;
import org.springframework.web.bind.annotation.*;

import com.APISynIq.ApiResolver.Entity.EndpointDataEntity;
import com.APISynIq.ApiResolver.Service.SynIqDataService;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/SynIq/api")
public class Controller {

    private final SynIqDataService synIqDataService;
    public Controller(SynIqDataService synIqDataService) {
        this.synIqDataService = synIqDataService;
    }

    @GetMapping("/active")
    public boolean activeCheck(){
        return true;
    }

    @PostMapping("/save")
    public String postMethodName(@RequestBody EndpointDataEntity entity) {
        System.out.println(entity);
        return "Data saved with ID: " + synIqDataService.save(entity.toGrpcEndpointData()).join().getId();
    }

    @PostMapping("/searchMatchesForBoth")
    public InputsAndReturnsMatch search(@RequestBody Query query) {
        return synIqDataService.queryForBoth(query.getQuery(), query.getLimit());
    }


    @PostMapping("/searchMatchesForInputDescrition")
    public List<EndpointDataEntity> searchForInputDes(@RequestBody Query query) {
        List<EndpointData> endpoints = synIqDataService.inputsDesMatch(query.getQuery(), query.getLimit());
        List<EndpointDataEntity> results = new ArrayList<>();
        for (EndpointData endpointData : endpoints) {
            EndpointDataEntity endpointDataEntity = new EndpointDataEntity();
            endpointDataEntity.grpcToEntity(endpointData);
            results.add(endpointDataEntity);
        }
        return results;
    }

    @PostMapping("/searchMatchesForReturnDescription")
    public List<EndpointDataEntity> searchForReturnDes(@RequestBody Query query) {
        List<EndpointData> endpoints = synIqDataService.returnDesMatch(query.getQuery(), query.getLimit());
        List<EndpointDataEntity> results = new ArrayList<>();
        for (EndpointData endpointData : endpoints) {
            EndpointDataEntity endpointDataEntity = new EndpointDataEntity();
            endpointDataEntity.grpcToEntity(endpointData);
            results.add(endpointDataEntity);
        }
        return results;
    }
}
