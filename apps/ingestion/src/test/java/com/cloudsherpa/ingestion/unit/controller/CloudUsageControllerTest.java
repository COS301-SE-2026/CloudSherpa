package com.cloudsherpa.ingestion.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.AwsCurIngestionService;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.GcpBillingIngestionService;
import com.cloudsherpa.ingestion.controller.CloudUsageController;
import com.cloudsherpa.ingestion.models.IngestionResult;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CloudUsageController.class)
class CloudUsageControllerTest {

  @SpringBootApplication(scanBasePackages = "com.cloudsherpa.ingestion.controller")
  static class TestApp {}

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CloudUsageService service;
  @MockitoBean private AwsCurIngestionService awsCurIngestionService;
  @MockitoBean private GcpBillingIngestionService gcpBillingIngestionService;

  @Test
  void ingestEndpointShouldReturn200() throws Exception {

    when(service.ingest(any())).thenReturn(new IngestionResult(List.of(), List.of()));

    mockMvc
        .perform(
            post("/api/events/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
            {
              "includeUsage": true,
              "includeBilling": false,
              "scopes": []
            }
            """))
        .andExpect(status().isOk());
  }

  @Test
  void ingestMockEndpointShouldReturn200() throws Exception {

    when(service.ingestMock(any())).thenReturn(new IngestionResult(List.of(), List.of()));

    mockMvc
        .perform(
            post("/api/events/ingest/mock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
            {
              "includeUsage": true,
              "includeBilling": false,
              "scopes": []
            }
            """))
        .andExpect(status().isOk());
  }

  @Test
  void ingestMockNoiseEndpointShouldReturn200() throws Exception {

    when(service.ingestMockWithNoise(any())).thenReturn(new IngestionResult(List.of(), List.of()));

    mockMvc
        .perform(
            post("/api/events/ingest/mockNoise")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
            {
              "includeUsage": true,
              "includeBilling": false,
              "scopes": []
            }
            """))
        .andExpect(status().isOk());
  }

  @Test
  void ingestEndpointShouldRejectInvalidJson() throws Exception {

    mockMvc
        .perform(
            post("/api/events/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("INVALID_JSON"))
        .andExpect(status().isBadRequest());
  }
}
