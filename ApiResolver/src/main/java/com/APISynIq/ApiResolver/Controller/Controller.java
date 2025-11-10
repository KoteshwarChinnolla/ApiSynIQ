package com.APISynIq.ApiResolver.Controller;

import com.apisyniq.grpc.InputsAndReturnsMatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.APISynIq.ApiResolver.Entity.SynIqData;
import com.APISynIq.ApiResolver.Service.SynIqDataService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@RestController
@RequestMapping("/api")
public class Controller {

    private final SynIqDataService synIqDataService;
    public Controller(SynIqDataService synIqDataService) {
        this.synIqDataService = synIqDataService;
    }

    @PostMapping("/save")
    public String postMethodName(@RequestBody SynIqData entity) {
        return "Data saved with ID: " + synIqDataService.save(entity.toGrpcInputData()).join().getId();
    }

    @PostMapping("/searchMatchesForBoth")
    public InputsAndReturnsMatch search(@RequestBody String entity) {
        return synIqDataService.queryForBoth(entity);
    }

    @PostMapping("/searchMatchesForInputDescrition")
    public List<SynIqData> searchForInputDes(@RequestBody String entity) { return synIqDataService.inputsDesMatch(entity);}

    @PostMapping("/searchMatchesForReturnDescription")
    public List<SynIqData> searchForReturnDes(@RequestBody String entity) { return synIqDataService.returnDesMatch(entity);}

}
