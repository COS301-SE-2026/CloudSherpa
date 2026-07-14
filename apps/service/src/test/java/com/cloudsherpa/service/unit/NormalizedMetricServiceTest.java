package com.cloudsherpa.service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.projections.AggregatedMetric;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import com.cloudsherpa.service.analytics.service.NormalizedMetricService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NormalizedMetricServiceTest {

  @Mock private NormalizedMetricsRepository normalizedMetricsRepository;

  @InjectMocks private NormalizedMetricService normalizedMetricService;

  @Test
  void fetchHistoricalDataReturnsAggregatedMetricsBetweenDates() {
    String from = "2026-04-01T00:00:00Z";
    String to = "2026-04-30T00:00:00Z";

    OffsetDateTime parsedFrom = OffsetDateTime.parse(from);
    OffsetDateTime parsedTo = OffsetDateTime.parse(to);

    AggregatedMetric aggregatedMetric = mock(AggregatedMetric.class);
    List<AggregatedMetric> expected = List.of(aggregatedMetric);

    when(normalizedMetricsRepository.findAggregatedMetricsByPeriod(parsedFrom, parsedTo, "1 day"))
        .thenReturn(expected);

    List<AggregatedMetric> actual = normalizedMetricService.fetchHistoricalData(from, to, "daily");

    assertEquals(expected, actual);
  }

  @Test
  void fetchHistoricalDataMapsWeeklyIntervalToOneWeekBucket() {
    String from = "2026-04-01T00:00:00Z";
    String to = "2026-04-30T00:00:00Z";

    OffsetDateTime parsedFrom = OffsetDateTime.parse(from);
    OffsetDateTime parsedTo = OffsetDateTime.parse(to);

    AggregatedMetric aggregatedMetric = mock(AggregatedMetric.class);
    List<AggregatedMetric> expected = List.of(aggregatedMetric);

    when(normalizedMetricsRepository.findAggregatedMetricsByPeriod(parsedFrom, parsedTo, "1 week"))
        .thenReturn(expected);

    List<AggregatedMetric> actual = normalizedMetricService.fetchHistoricalData(from, to, "weekly");

    assertEquals(expected, actual);
  }

  @Test
  void fetchHistoricalDataMapsMonthlyIntervalToOneMonthBucket() {
    String from = "2026-04-01T00:00:00Z";
    String to = "2026-04-30T00:00:00Z";

    OffsetDateTime parsedFrom = OffsetDateTime.parse(from);
    OffsetDateTime parsedTo = OffsetDateTime.parse(to);

    AggregatedMetric aggregatedMetric = mock(AggregatedMetric.class);
    List<AggregatedMetric> expected = List.of(aggregatedMetric);

    when(normalizedMetricsRepository.findAggregatedMetricsByPeriod(parsedFrom, parsedTo, "1 month"))
        .thenReturn(expected);

    List<AggregatedMetric> actual =
        normalizedMetricService.fetchHistoricalData(from, to, "monthly");

    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @CsvSource({
    "2026-04-15T00:00:00Z,2026-04-05T00:00:00Z",
    "2026-04-15T00:00:0,2026-04-05T00:00:00Z",
    "2026-04-15T00:00:00Z,2026-04-05T00::00Z"
  })
  void throwExceptionWhenInvalidDate(String from, String to) {
    assertThrows(
        Exception.class, () -> normalizedMetricService.fetchHistoricalData(from, to, "daily"));
  }

  @Test
  void throwExceptionWhenIntervalInvalid() {
    assertThrows(
        ResponseStatusException.class,
        () ->
            normalizedMetricService.fetchHistoricalData(
                "2026-04-01T00:00:00Z", "2026-04-30T00:00:00Z", "yearly"));
  }
}
