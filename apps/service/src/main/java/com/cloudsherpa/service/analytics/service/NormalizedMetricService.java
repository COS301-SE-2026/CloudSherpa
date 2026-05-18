package com.cloudsherpa.service.analytics.service;

import com.cloudsherpa.service.analytics.entities.NormalizedMetrics;
import com.cloudsherpa.service.analytics.repository.NormalizedMetricsRepository;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NormalizedMetricService {
  private final NormalizedMetricsRepository normalizedMetricsRepository;

  NormalizedMetricService(NormalizedMetricsRepository normalizedMetricsRepository) {
    this.normalizedMetricsRepository = normalizedMetricsRepository;
  }

  public List<NormalizedMetrics> fetchHistoricalData(String from, String to) throws Exception {

    OffsetDateTime parsedFromDate;
    OffsetDateTime parsedToDate;

    try {
      parsedFromDate = OffsetDateTime.parse(from);
      parsedToDate = OffsetDateTime.parse(to);
    } catch (DateTimeParseException e) {
      // Still need to log for server-side visibility
      throw new Exception("Date Strings do not conform to ISO-8601 standard");
    }

    if (parsedFromDate.isAfter(parsedToDate)) {
      // from after to
      throw new Exception("Invalid interval");
    }

    return normalizedMetricsRepository.findByRecordedAtBetween(parsedFromDate, parsedToDate);
  }
}
