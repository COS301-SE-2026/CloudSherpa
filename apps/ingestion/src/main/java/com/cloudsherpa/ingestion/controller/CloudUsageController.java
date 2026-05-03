package com.cloudsherpa.ingestion.controller;

import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class CloudUsageController {
  private final CloudUsageService cloudUsageService;

  // CloudUsageService injected as dependency of CloudUsageController
  public CloudUsageController(CloudUsageService cloudUsageService) {
    this.cloudUsageService = cloudUsageService;
  }

  // POST even though no request body, produce artifact (metric) => POST
  @PostMapping("/mock")
  public NormalizedMetric sendMockEvent() {
    return cloudUsageService.sendMockEvent();
  }
}
