package com.APISynIq.ApiResolver.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.APISynIq.ApiResolver.Entity.SynIqData;
import com.APISynIq.ApiResolver.Service.SynIqDataService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api")
public class Controller {
    @Autowired
    SynIqDataService synIqDataService;

    @PostMapping("/postSomething")
    public String postMethodName(@RequestBody SynIqData entity) {
        return "Data saved with ID: " + synIqDataService.save(entity).getId();
    }
    
}
