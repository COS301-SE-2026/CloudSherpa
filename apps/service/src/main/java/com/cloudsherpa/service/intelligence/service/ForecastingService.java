package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import com.cloudsherpa.service.intelligence.dto.BillingForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.ResourceUsageForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.ResourceUsageForecastResponseDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ForecastingService {
  private final NormalizedMetricsRepository normalizedMetricsRepository;
  private final NormalizedCostsRepository normalizedCostsRepository;
  private final Sampler sampler;
  private final RestClient restClient;

  private final Logger logger = LoggerFactory.getLogger(ForecastingService.class);

  // These values are tailored for the Chronos-2 foundation model, if we start to support other
  // models/methods
  // these values can be changed from constants to members and then determined dynamically
  private static final int CONTEXT_LENGTH = 8092;
  private static final int FORECAST_LENGTH = 1024; // NOSONAR: wip

  public ForecastingService(
      NormalizedMetricsRepository normalizedMetricsRepository,
      NormalizedCostsRepository normalizedCostsRepository,
      Sampler sampler,
      RestClient restClient) {
    this.normalizedMetricsRepository = normalizedMetricsRepository;
    this.normalizedCostsRepository = normalizedCostsRepository;
    this.sampler = sampler;
    this.restClient = restClient;
  }

  public ResourceUsageForecastResponseDto forecastUsage(
      ResourceUsageForecastRequestDto resourceUsageForecastRequestDto) {

    List<TimestampedNumericDataPoint> timestampedNumericDataPoints =
        normalizedMetricsRepository.getTimestampedMetricValues(
            resourceUsageForecastRequestDto.resourceId(),
            resourceUsageForecastRequestDto.metricType(),
            PageRequest.of(0, CONTEXT_LENGTH));

    debugDataPointLog(timestampedNumericDataPoints);

    List<TimestampedNumericDataPoint> sanitizedNumericDataPoints =
        sampler.sample(timestampedNumericDataPoints, true);

    List<Instant> timestamps = new ArrayList<>();
    List<BigDecimal> values = new ArrayList<>();

    for (TimestampedNumericDataPoint timestampedNumericDataPoint : sanitizedNumericDataPoints) {
      timestamps.addLast(timestampedNumericDataPoint.timestamp());
      values.addLast(timestampedNumericDataPoint.value());
    }

    IntelligenceForecastRequestDto intelligenceForecastRequestDto =
        new IntelligenceForecastRequestDto(
            FORECAST_LENGTH, timestamps, values, "chronos_univariate");

    IntelligenceForecastResponseDto intelligenceForecastResponseDto =
        restClient
            .post()
            .uri("/forecast-chronos")
            .contentType(MediaType.APPLICATION_JSON)
            .body(intelligenceForecastRequestDto)
            .retrieve()
            .body(IntelligenceForecastResponseDto.class);

    return null;
  }

  public BillingForecastResponseDto forecastBilling(
      BillingForecastRequestDto billingForecastRequestDto) {
    List<TimestampedNumericDataPoint> timestampedNumericDataPoints = new ArrayList<>();
    for (String chargeId : billingForecastRequestDto.chargeIds()) {
      timestampedNumericDataPoints.addAll(
          normalizedCostsRepository.getTimestampedBillingValues(
              chargeId, PageRequest.of(0, CONTEXT_LENGTH)));
    }

    debugDataPointLog(timestampedNumericDataPoints);

    return null;
  }

  private void debugDataPointLog(List<TimestampedNumericDataPoint> timestampedNumericDataPoints) {
    if (timestampedNumericDataPoints.isEmpty()) {
      logger.info("No data");
    }

    for (TimestampedNumericDataPoint timestampedNumericDataPoint : timestampedNumericDataPoints) {
      logger.info(
          "Value: {} Timestamp {}",
          timestampedNumericDataPoint.value(),
          timestampedNumericDataPoint.timestamp());
    }
  }
}
