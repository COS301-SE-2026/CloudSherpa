package com.cloudsherpa.service.scheduler;

import com.cloudsherpa.service.scheduler.dto.IngestionRequestEvent;
import com.cloudsherpa.service.scheduler.dto.IngestionResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UsageIngestionClient {

  private final RestClient restClient;

  public UsageIngestionClient(RestClient restClient) {
    this.restClient = restClient;
  }

  public IngestionResult ingest(IngestionRequestEvent request) {

    return restClient
        .post()
        .uri("/api/events/ingest")
        .body(request)
        .retrieve()
        .body(IngestionResult.class);
  }
}
