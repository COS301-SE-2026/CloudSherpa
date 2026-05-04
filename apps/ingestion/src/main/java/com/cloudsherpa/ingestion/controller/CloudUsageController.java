package com.cloudsherpa.ingestion.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.IngestionResult;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/events")
public class CloudUsageController {
    private final CloudUsageService cloudUsageService;

    // CloudUsageService injected as dependency of CloudUsageController
    public CloudUsageController(CloudUsageService cloudUsageService) {
        this.cloudUsageService = cloudUsageService;
    }

    @PostMapping("/ingest")
    public IngestionResult ingest(IngestionRequestEvent request) {
        return cloudUsageService.ingest(request);
    }

    // POST even though no request body, produce artifact (metric) => POST
    @PostMapping("/mock")
    public NormalizedMetric sendMockEvent() {
        return cloudUsageService.sendMockEvent();
    }

}
