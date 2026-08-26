package com.cloudsherpa.service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.projections.AggregatedMetric;
import com.cloudsherpa.lib.projections.ResourceNames;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.analytics.dto.AggregatedMetricDto;
import com.cloudsherpa.service.analytics.service.NormalizedMetricService;
import com.cloudsherpa.service.metrics.MetricDisplayNameMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

  @Mock private MetricDisplayNameMapper metricMapper;

  @Mock private ResourceRepository resourceRepository;

  @InjectMocks private NormalizedMetricService normalizedMetricService;

  @Test
  void fetchHistoricalDataReturnsMetricsBetweenDates() {
    String from = "2026-04-01T00:00:00Z";
    String to = "2026-04-30T00:00:00Z";

    OffsetDateTime parsedFrom = OffsetDateTime.parse(from);
    OffsetDateTime parsedTo = OffsetDateTime.parse(to);
    Instant now = Instant.now();
    UUID resourceId = UUID.randomUUID();

    AggregatedMetric aggregatedMetric = mock(AggregatedMetric.class);
    when(aggregatedMetric.getResourceId()).thenReturn(resourceId);
    when(aggregatedMetric.getMetricType()).thenReturn("usage");
    when(aggregatedMetric.getMetricName()).thenReturn("raw.metric.string");
    when(aggregatedMetric.getMetricValue()).thenReturn(BigDecimal.TEN);
    when(aggregatedMetric.getUnit()).thenReturn("bytes");
    when(aggregatedMetric.getPeriodStart()).thenReturn(now);
    when(aggregatedMetric.getPeriodEnd()).thenReturn(now);
    when(aggregatedMetric.getSampleCount()).thenReturn(1L);

    when(metricMapper.toDisplayName("raw.metric.string")).thenReturn("Clean UI Name");

    List<AggregatedMetric> repositoryResult = List.of(aggregatedMetric);

    when(normalizedMetricsRepository.findAggregatedMetricsByPeriod(parsedFrom, parsedTo, "1 day"))
        .thenReturn(repositoryResult);

    AggregatedMetricDto expectedDto =
        new AggregatedMetricDto(
            resourceId,
            "usage",
            "Clean UI Name",
            BigDecimal.TEN,
            "bytes",
            now.atOffset(ZoneOffset.UTC),
            now.atOffset(ZoneOffset.UTC),
            1L);
    List<AggregatedMetricDto> expected = List.of(expectedDto);

    List<AggregatedMetricDto> actual =
        normalizedMetricService.fetchHistoricalData(from, to, "daily");

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
  void fetchResourceNames() throws ResponseStatusException {

    UUID id = UUID.randomUUID();

    ResourceNames resourceName = mock(ResourceNames.class);
    when(resourceName.getId()).thenReturn(id);
    when(resourceName.getResourceType()).thenReturn("EC2");

    when(resourceRepository.findResourceNames()).thenReturn(List.of(resourceName));

    Map<String, String> actual = normalizedMetricService.fetchResourceNames();

    Map<String, String> expected = Map.of(id.toString(), "EC2");

    assertEquals(expected, actual);
  }

  @Test
  void returnsEmptyMapWhenNoResourceNames() {
    when(resourceRepository.findResourceNames()).thenReturn(List.of());

    Map<String, String> actual = normalizedMetricService.fetchResourceNames();

    assertEquals(Map.of(), actual);
  }
}
