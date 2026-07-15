package com.cloudsherpa.ingestion.controller;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.AwsCurIngestionService;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.IngestionResult;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Cloud Usage Ingestion",
    description = "Endpoints for ingesting cloud usage and billing data from cloud providers")
@RestController
@RequestMapping("/api/events")
public class CloudUsageController {
  private final CloudUsageService cloudUsageService;
  private final AwsCurIngestionService awsCurIngestionService;

  // CloudUsageService injected as dependency of CloudUsageController
  public CloudUsageController(
      CloudUsageService cloudUsageService, AwsCurIngestionService awsCurIngestionService) {
    this.cloudUsageService = cloudUsageService;
    this.awsCurIngestionService = awsCurIngestionService;
  }

  @Operation(
      summary = "Ingest real cloud usage and billing data",
      description =
          "Retrieves cloud usage and/or billing data from configured cloud providers "
              + "using the supplied credentials, account scopes, and time range.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cloud data successfully ingested",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = IngestionResult.class)))
      })
  @PostMapping("/ingest")
  public IngestionResult ingest(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Cloud ingestion request object",
              required = true,
              content = @Content(schema = @Schema(implementation = IngestionRequestEvent.class)))
          @RequestBody
          IngestionRequestEvent request) {
    return cloudUsageService.ingest(request);
  }

  @Operation(
      summary = "Ingest mock cloud data with noise",
      description =
          "Generates and ingests mock cloud usage data using Reinstein Uhlenbeck noise "
              + "for analytics, anomaly detection, and testing.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Mock cloud data successfully ingested",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = IngestionResult.class)))
      })
  @PostMapping("/ingest/mockNoise")
  public IngestionResult ingestMockWithNoise(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "Ingestion request object, credentials and some other fields are not used",
              required = true,
              content = @Content(schema = @Schema(implementation = IngestionRequestEvent.class)))
          @RequestBody
          IngestionRequestEvent request) {
    return cloudUsageService.ingestMockWithNoise(request);
  }

  @Operation(
      summary = "Ingest deterministic fixed-size mock cloud data",
      description =
          "Generates and ingests deterministic mock cloud usage data"
              + "for development and integration testing.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Mock cloud data successfully ingested",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = IngestionResult.class))),
      })
  @PostMapping("/ingest/mock")
  public IngestionResult ingestMock(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "Ingestion request object, credentials and some other fields are not used",
              required = true,
              content = @Content(schema = @Schema(implementation = IngestionRequestEvent.class)))
          @RequestBody
          IngestionRequestEvent request) {
    return cloudUsageService.ingestMock(request);
  }

  @PostMapping("/ingest/aws/billing/cur")
  public void ingestAwsBillingCur() {
    awsCurIngestionService.runCurIngestion();
  }
}
