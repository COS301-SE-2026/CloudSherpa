package com.cloudsherpa.service.intelligence.controller;

import com.cloudsherpa.service.intelligence.dto.ResourceUsageForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.ResourceUsageForecastResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/intelligence/forecasting")
@Tag(name = "Intelligence", description = "CloudSherpa Forecasting Intelligence Operations")
public class IntelligenceForecastingController {

  @Operation(
      summary = "Forecast resource usage",
      description =
          "Generates predicted values and prediction intervals for a resource metric up to the requested forecast horizon")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resource usage forecast generated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResourceUsageForecastResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "The forecast request is malformed or contains invalid values",
            content = @Content),
        @ApiResponse(
            responseCode = "422",
            description = "Insufficient historical data available to make forecasting prediction",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "Resource does not exist or MetricType is not applicable to Resource")
      })
  @PostMapping("/resource")
  public ResponseEntity<ResourceUsageForecastResponseDto> resourceUsageForecast(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "The resource, metric, and timestamp through which usage should be forecast",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = ResourceUsageForecastRequestDto.class)))
          @RequestBody
          ResourceUsageForecastRequestDto request) {

    // mock response to show structure
    List<Instant> mockTimestamps =
        List.of(
            Instant.parse("2026-08-03T08:00:00Z"),
            Instant.parse("2026-08-03T09:00:00Z"),
            Instant.parse("2026-08-03T10:00:00Z"));

    List<Double> predictedValues = List.of(0.42, 0.47, 0.51);

    List<Double> q1 = List.of(0.36, 0.40, 0.44);

    List<Double> q2 = List.of(0.49, 0.54, 0.59);

    ResourceUsageForecastResponseDto mockResponse =
        new ResourceUsageForecastResponseDto(mockTimestamps, predictedValues, q1, q2);
    return ResponseEntity.status(HttpStatus.OK).body(mockResponse);
  }
}
