package com.cloudsherpa.ingestion.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudsherpa.ingestion.service.CloudUsageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/events")
public class CloudUsageController {
    private final CloudUsageService producerService;

    // CloudUsageService injected as dependency of CloudUsageController
    public CloudUsageController(CloudUsageService producerService) {
        this.producerService = producerService;
    }

    // POST even though no request body, produce artifact (event) => POST
    @PostMapping("/mock")
    public String sendMockEvent() {
        
        producerService.sendMockEvent();
        return "Mock event sent";
    }
    
}
