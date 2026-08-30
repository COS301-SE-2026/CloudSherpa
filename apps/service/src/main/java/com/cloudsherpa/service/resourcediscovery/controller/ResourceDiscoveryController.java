package com.cloudsherpa.service.resourcediscovery.controller;

import com.cloudsherpa.service.resourcediscovery.dto.ResourceDetailDto;
import com.cloudsherpa.service.resourcediscovery.dto.ResourceDiscoveryDto;
import com.cloudsherpa.service.resourcediscovery.service.ResourceDiscoveryService;
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
        "Endpoints for discovering cloud services, resources, and generating permissions policies")
@RestController
@RequestMapping("/api/cloud-resources")
public class ResourceDiscoveryController {

  private final ResourceDiscoveryService service;

  public ResourceDiscoveryController(ResourceDiscoveryService service) {
    this.service = service;
  }

  @Operation(
      summary = "Get available cloud services",
      description = "Retrieves a list of all supported services for a given cloud provider")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Services successfully returned",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = List.class)))
      })
  @PostMapping("/services")
  public List<String> getServices(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Cloud provider name (e.g. aws, azure, gcp)",
              required = true,
              content = @Content(schema = @Schema(implementation = String.class)))
          @RequestBody
          String provider) {

    return service.getServices(provider);
  }

  @Operation(
      summary = "Get cloud resources",
      description =
          "Retrieves all discoverable resources for the specified cloud provider using the supplied credentials")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resources successfully returned",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResourceDetailDto.class)))
      })
  @PostMapping("/resources/{provider}")
  public List<ResourceDetailDto> getResources(
      @PathVariable String provider, @RequestBody ResourceDiscoveryDto request) {

    return service.getResources(provider, request);
  }

  @Operation(
      summary = "Generate AWS IAM policy",
      description = "Generates a least-privilege AWS IAM policy for the selected AWS services")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "IAM policy successfully generated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class)))
      })
  @PostMapping("/aws/permissions")
  public String generateAwsPermissions(@RequestBody List<String> services) {

    return service.generateAwsPermissions(services);
  }

  @Operation(
      summary = "Generate GCP permissions list",
      description =
          "Generates a least-privilege GCP permissions list for the selected GCP services")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions list successfully generated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = List.class)))
      })
  @PostMapping("/gcp/permissions")
  public List<String> generateGcpPermissions(@RequestBody List<String> services) {

    return service.generateGcpPermissions(services);
  }

  @Operation(
      summary = "Generate Azure permissions list",
      description =
          "Generates a least-privilege Azure permissions list for the selected Azure services")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions list successfully generated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = List.class)))
      })
  @PostMapping("/azure/permissions")
  public List<String> generateAzurePermissions(@RequestBody List<String> services) {

    return service.generateAzurePermissions(services);
  }
}
