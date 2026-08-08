package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.service.intelligence.dto.BillingForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BillingForecastingService extends ForecastingService {
  private final NormalizedCostsRepository normalizedCostsRepository;

  public BillingForecastingService(
      NormalizedCostsRepository normalizedCostsRepository, RestClient restClient, Sampler sampler) {
    super(restClient, sampler);
    this.normalizedCostsRepository = normalizedCostsRepository;
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
}
