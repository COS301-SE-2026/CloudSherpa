package com.cloudsherpa.service.intelligence.controller;

import com.cloudsherpa.service.intelligence.dto.BillingForecastIndividualChargesRequestDto;
import com.cloudsherpa.service.intelligence.dto.BillingForecastRequest;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.ResourceUsageForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.ResourceUsageForecastResponseDto;
import com.cloudsherpa.service.intelligence.service.billing.BillingForecastValue;
import com.cloudsherpa.service.intelligence.service.billing.BillingIntelligenceService;
import com.cloudsherpa.service.intelligence.service.usage.UsageForecastingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
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

  private final BillingIntelligenceService billingIntelligenceService;
  private final UsageForecastingService usageForecastingService;
  private final boolean useMockForecasting;

  public IntelligenceForecastingController(
      UsageForecastingService usageForecastingService,
      @Value("${intelligence.forecasting.mock:false}") boolean useMockForecasting,
      BillingIntelligenceService billingIntelligenceService) {
    this.usageForecastingService = usageForecastingService;
    this.useMockForecasting = useMockForecasting;
    this.billingIntelligenceService = billingIntelligenceService;
  }

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
            description = "Resource does not exist or MetricType is not applicable to Resource",
            content = @Content)
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
    if (useMockForecasting) {
      return ResponseEntity.ok(mockResourceUsageForecast());
    }

    return ResponseEntity.ok().body(usageForecastingService.forecastUsage(request));
  }

  @Operation(
      summary = "Forecast Billing with Charges",
      description =
          "Generates cumalative cost predection value and analytics for list of charges"
              + " of which the List can contain a single or multiple charges")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Billing forecast generated succesfully.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BillingForecastResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "The billing forecast request is malformed or contains invalid values",
            content = @Content),
        @ApiResponse(
            responseCode = "422",
            description =
                "Insufficient historical data available to make any forecasting prediction",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "None of the charges found",
            content = @Content)
      })
  @PostMapping("/billing-charges")
  public ResponseEntity<BillingForecastResponseDto> billingForecastWithCharges(
      // @io.swagger.v3.oas.annotations.parameters.RequestBody
      @RequestBody BillingForecastIndividualChargesRequestDto request) {

    if (useMockForecasting) {
      String mockChargeId = "mock-charge-id";
      BillingForecastResponseDto mockResponse =
          new BillingForecastResponseDto(
              BigDecimal.valueOf(42.50),
              BigDecimal.ZERO,
              Map.of(
                  mockChargeId,
                  new BillingForecastValue(
                      BigDecimal.valueOf(42.5), BigDecimal.valueOf(100), mockChargeId)),
              List.of(),
              BigDecimal.ZERO,
              BigDecimal.valueOf(42.5),
              mockChargeId);

      return ResponseEntity.status(HttpStatus.OK).body(mockResponse);
    } else {
      return ResponseEntity.ok()
          .body(billingIntelligenceService.processSelectCharges(request, Instant.now()));
    }
  }

  @Operation(
      summary = "Forecast Billing for all non-credit charges",
      description =
          "Generates cumalative cost predection value and analytics for all non-credit charges")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Billing forecast generated succesfully.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BillingForecastResponseDto.class))),
        @ApiResponse(
            responseCode = "422",
            description =
                "Insufficient historical data available to make any forecasting prediction",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "No non-credit charges found",
            content = @Content)
      })
  @PostMapping("/billing")
  public ResponseEntity<BillingForecastResponseDto> billingForecast(
      @RequestBody BillingForecastRequest request) {

    return ResponseEntity.ok()
        .body(billingIntelligenceService.processAllCharges(request, Instant.now()));
  }

  private ResourceUsageForecastResponseDto mockResourceUsageForecast() {
    List<LocalDateTime> mockTimestamps =
        List.of(
            LocalDateTime.parse("2026-08-03T08:00:00"),
            LocalDateTime.parse("2026-08-03T09:00:00"),
            LocalDateTime.parse("2026-08-03T10:00:00"));

    List<BigDecimal> predictedValues =
        List.of(BigDecimal.valueOf(0.42), BigDecimal.valueOf(0.47), BigDecimal.valueOf(0.51));

    List<BigDecimal> q1 =
        List.of(BigDecimal.valueOf(0.36), BigDecimal.valueOf(0.40), BigDecimal.valueOf(0.44));

    List<BigDecimal> q3 =
        List.of(BigDecimal.valueOf(0.49), BigDecimal.valueOf(0.54), BigDecimal.valueOf(0.59));

    return new ResourceUsageForecastResponseDto(mockTimestamps, predictedValues, q1, q3);
  }
}
