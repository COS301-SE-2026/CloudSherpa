package com.cloudsherpa.ingestion.controller;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.AwsCurIngestionService;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline.AwsCurContext;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.GcpBillingIngestionService;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.IngestionResult;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  private final Environment environment;
  private final GcpBillingIngestionService gcpBillingIngestionService;

  @Value("${dev.gcp.billing_config_id:}")
  private String devGcpBillingConfigId;

  // CloudUsageService injected as dependency of CloudUsageController
  public CloudUsageController(
      CloudUsageService cloudUsageService,
      AwsCurIngestionService awsCurIngestionService,
      Environment environment,
      GcpBillingIngestionService gcpBillingIngestionService) {
    this.cloudUsageService = cloudUsageService;
    this.awsCurIngestionService = awsCurIngestionService;
    this.environment = environment;
    this.gcpBillingIngestionService = gcpBillingIngestionService;
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

  @Operation(
      summary = "Trigger AWS CUR billing ingestion with credentials stored for test user",
      description =
          "When database is seeded appropriately, ingest a billing report for testing purposes")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "AWS CUR billing ingestion ran successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AwsCurContext.class)))
      })
  @PostMapping("/ingest/aws/billing/cur")
  public AwsCurContext ingestAwsBillingCur() {
    return awsCurIngestionService.execute(
        "5ebe4340-c5ec-4833-ad93-06abf4609f03", "e0000000-0000-0000-0000-000000000001");
  }

  @Operation(
      summary = "Trigger GCP BigQuery billing ingestion manually",
      description =
          "Runs GCP BigQuery billing ingestion for testing and development. "
              + "This endpoint is only available when the dev Spring profile is active.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "GCP BigQuery billing ingestion ran successfully",
            content = @Content),
        @ApiResponse(
            responseCode = "401",
            description = "Endpoint is only available in the dev profile",
            content = @Content)
      })
  @PostMapping("ingest/gcp/billing/bigquery")
  public ResponseEntity<String> ingestGcpBigqueryBilling() {
    if (!environment.matchesProfiles("dev")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    if (devGcpBillingConfigId == null || devGcpBillingConfigId.isBlank()) {
      return ResponseEntity.badRequest().body("Environment misconfigured");
    }

    gcpBillingIngestionService.execute(
        UUID.fromString("5ebe4340-c5ec-4833-ad93-06abf4609f03"),
        UUID.fromString(devGcpBillingConfigId));

    return ResponseEntity.ok().build();
  }
}
