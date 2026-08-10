package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.ResourceUsageForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.ResourceUsageForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.SanatizedSeries;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsageForecastingService extends ForecastingService {
  private final NormalizedMetricsRepository normalizedMetricsRepository;

  public UsageForecastingService(
      NormalizedMetricsRepository normalizedMetricsRepository,
      Sampler sampler,
      RestClient restClient) {
    super(restClient, sampler);
    this.normalizedMetricsRepository = normalizedMetricsRepository;
  }

  public ResourceUsageForecastResponseDto forecastUsage(
      ResourceUsageForecastRequestDto resourceUsageForecastRequestDto) {

    List<TimestampedNumericDataPoint> timestampedNumericDataPoints =
        getUsageSeries(resourceUsageForecastRequestDto);

    IntelligenceForecastResponseDto intelligenceForecastResponseDto =
        executeUsageForecastPipeline(timestampedNumericDataPoints);

    if (intelligenceForecastResponseDto == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "Forecasting service returned empty response");
    }

    return new ResourceUsageForecastResponseDto(
        intelligenceForecastResponseDto.timestamps(),
        intelligenceForecastResponseDto.forecast(),
        intelligenceForecastResponseDto.q1(),
        intelligenceForecastResponseDto.q3());
  }

  private List<TimestampedNumericDataPoint> getUsageSeries(
      ResourceUsageForecastRequestDto resourceUsageForecastRequestDto) {
    return normalizedMetricsRepository.getTimestampedMetricValues(
        resourceUsageForecastRequestDto.resourceId(),
        resourceUsageForecastRequestDto.metricType(),
        PageRequest.of(0, CONTEXT_LENGTH));
  }

  private IntelligenceForecastResponseDto executeUsageForecastPipeline(
      List<TimestampedNumericDataPoint> timestampedNumericDataPoints) {
    SanatizedSeries sanitizedNumericDataPoints = sampler.sample(timestampedNumericDataPoints, true);
    IntelligenceForecastRequestDto intelligenceForecastRequestDto =
        constructForecastRequest(
            sanitizedNumericDataPoints.timestampedNumericDataPoints(), FORECAST_LENGTH);
    return makeForecastRequest(intelligenceForecastRequestDto);
  }
}
