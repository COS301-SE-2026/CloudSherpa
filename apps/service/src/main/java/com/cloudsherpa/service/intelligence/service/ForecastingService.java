package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastResponseDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public abstract class ForecastingService {
  private final RestClient restClient;
  protected final Sampler sampler;

  private final Logger logger = LoggerFactory.getLogger(ForecastingService.class);

  // These values are tailored for the Chronos-2 foundation model, if we start to support other
  // models/methods
  // these values can be changed from constants to members and then determined dynamically
  protected static final int CONTEXT_LENGTH = 8092;
  protected static final int FORECAST_LENGTH = 1024;

  protected ForecastingService(RestClient restClient, Sampler sampler) {
    this.restClient = restClient;
    this.sampler = sampler;
  }

  protected void debugDataPointLog(List<TimestampedNumericDataPoint> timestampedNumericDataPoints) {
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

  protected IntelligenceForecastRequestDto constructForecastRequest(
      List<TimestampedNumericDataPoint> sanatizedDataPoints, Integer forecastLength) {
    List<Instant> timestamps = new ArrayList<>();
    List<BigDecimal> values = new ArrayList<>();

    for (TimestampedNumericDataPoint timestampedNumericDataPoint : sanatizedDataPoints) {
      timestamps.addLast(timestampedNumericDataPoint.timestamp().truncatedTo(ChronoUnit.SECONDS));
      values.addLast(timestampedNumericDataPoint.value());
    }

    return new IntelligenceForecastRequestDto(
        forecastLength, timestamps, values, "chronos_univariate");
  }

  protected IntelligenceForecastResponseDto makeForecastRequest(
      IntelligenceForecastRequestDto intelligenceForecastRequestDto) {
    return restClient
        .post()
        .uri("/forecast-chronos")
        .contentType(MediaType.APPLICATION_JSON)
        .body(intelligenceForecastRequestDto)
        .retrieve()
        .body(IntelligenceForecastResponseDto.class);
  }
}
