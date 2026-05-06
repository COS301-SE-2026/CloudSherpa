package com.cloudsherpa.ingestion.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.IngestionResult;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/events")
public class CloudUsageController {
    private final CloudUsageService cloudUsageService;

    // CloudUsageService injected as dependency of CloudUsageController
    public CloudUsageController(CloudUsageService cloudUsageService) {
        this.cloudUsageService = cloudUsageService;
    }

    @PostMapping("/ingest")
    public IngestionResult ingest(@RequestBody IngestionRequestEvent request) {
        return cloudUsageService.ingest(request);
    }
}