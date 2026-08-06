package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import com.cloudsherpa.service.intelligence.dto.BillingForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.ResourceUsageForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.ResourceUsageForecastResponseDto;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ForecastingService {
  private final NormalizedMetricsRepository normalizedMetricsRepository;
  private final NormalizedCostsRepository normalizedCostsRepository;

  private final Logger logger = LoggerFactory.getLogger(ForecastingService.class);

  // These values are tailored for the Chronos-2 foundation model, if we start to support other
  // models/methods
  // these values can be changed from constants to members and then determined dynamically
  private static final int CONTEXT_LENGTH = 8092;
  private static final int FORECAST_LENGTH = 1024; // NOSONAR: wip

  public ForecastingService(
      NormalizedMetricsRepository normalizedMetricsRepository,
      NormalizedCostsRepository normalizedCostsRepository) {
    this.normalizedMetricsRepository = normalizedMetricsRepository;
    this.normalizedCostsRepository = normalizedCostsRepository;
  }

  public ResourceUsageForecastResponseDto forecastUsage(
      ResourceUsageForecastRequestDto resourceUsageForecastRequestDto) {

    List<TimestampedNumericDataPoint> timestampedNumericDataPoints =
        normalizedMetricsRepository.getTimestampedMetricValues(
            resourceUsageForecastRequestDto.resourceId(),
            resourceUsageForecastRequestDto.metricType(),
            PageRequest.of(0, CONTEXT_LENGTH));

    debugDataPointLog(timestampedNumericDataPoints);

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
