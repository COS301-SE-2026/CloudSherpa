package com.cloudsherpa.ingestion.controller;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.service.CloudResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Cloud Resource Discovery",
    description =
        "Endpoints for discovering which services and resources are available from cloud providers")
@RestController
@RequestMapping("/api/cloud-resources")
public class CloudResourceController {

  private final CloudResourceService cloudResourceService;

  public CloudResourceController(CloudResourceService cloudResourceService) {
    this.cloudResourceService = cloudResourceService;
  }

  @Operation(
      summary = "Get all supported services for a cloud provider",
      description =
          "Retrieves a list of all services that can be monitored by CloudSherpa for a given provider")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cloud services successfully returned",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = List.class)))
      })
  @PostMapping("/services")
  public List<String> getAllOfferedServices(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Cloud provider name (e.g. aws, azure, gcp)",
              required = true,
              content = @Content(schema = @Schema(implementation = String.class)))
          @RequestBody
          String provider) {

    return cloudResourceService.getAllOfferedServices(provider);
  }

  @Operation(
      summary = "Get all resources for a cloud provider",
      description =
          "Retrieves all discoverable resources for a cloud provider using the supplied credentials")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resources successfully returned",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResourceDetail.class)))
      })
  @PostMapping("/resources/{provider}")
  public List<ResourceDetail> getAllResources(
      @PathVariable String provider, @RequestBody CloudCredentials credentials) {

    return cloudResourceService.getAllResources(provider, credentials);
  }

  @Operation(
      summary = "Generate AWS IAM policy",
      description = "Generates a least-privilege AWS IAM policy for the selected services")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "IAM policy successfully generated")
      })
  @PostMapping("/aws/permissions")
  public String generateAwsPermissionsPolicy(@RequestBody List<String> services) {

    return cloudResourceService.generateAwsPermissionsPolicy(services);
  }
}
