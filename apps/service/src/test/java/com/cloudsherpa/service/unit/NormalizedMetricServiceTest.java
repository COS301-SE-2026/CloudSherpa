package com.cloudsherpa.service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.dtos.ResourceMetricEntry;
import com.cloudsherpa.lib.projections.AggregatedMetric;
import com.cloudsherpa.lib.projections.ResourceNames;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.analytics.dto.MetricDto;
import com.cloudsherpa.service.analytics.dto.ResourceMetricsGroupDto;
import com.cloudsherpa.service.analytics.model.ResourceMetric;
import com.cloudsherpa.service.analytics.service.NormalizedMetricService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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

  @Mock private ResourceRepository resourceRepository;

  @InjectMocks private NormalizedMetricService normalizedMetricService;

  @Mock private com.cloudsherpa.service.metrics.MetricDisplayNameMapper metricMapper;

  @Test
  void fetchHistoricalDataReturnsMetricsBetweenDates() {
    String from = "2026-04-01T00:00:00Z";
    String to = "2026-04-30T00:00:00Z";

    OffsetDateTime parsedFrom = OffsetDateTime.parse(from);
    OffsetDateTime parsedTo = OffsetDateTime.parse(to);

    UUID resourceId = UUID.randomUUID();
    java.time.Instant start = parsedFrom.toInstant();
    java.time.Instant end = parsedTo.toInstant();

    AggregatedMetric aggregatedMetric = mock(AggregatedMetric.class);
    when(aggregatedMetric.getResourceId()).thenReturn(resourceId);
    when(aggregatedMetric.getMetricType()).thenReturn("Gauge");
    when(aggregatedMetric.getMetricName()).thenReturn("raw_cpu_metric");
    when(aggregatedMetric.getMetricValue()).thenReturn(java.math.BigDecimal.TEN);
    when(aggregatedMetric.getUnit()).thenReturn("Percent");
    when(aggregatedMetric.getPeriodStart()).thenReturn(start);
    when(aggregatedMetric.getPeriodEnd()).thenReturn(end);
    when(aggregatedMetric.getSampleCount()).thenReturn(5L);

    when(metricMapper.toDisplayName("raw_cpu_metric")).thenReturn("CPU Utilization");

    when(normalizedMetricsRepository.findAggregatedMetricsByPeriod(parsedFrom, parsedTo, "1 day"))
        .thenReturn(List.of(aggregatedMetric));

    List<MetricDto> expected =
        List.of(
            new MetricDto(
                resourceId,
                "Gauge",
                "CPU Utilization",
                java.math.BigDecimal.TEN,
                "Percent",
                start.atOffset(java.time.ZoneOffset.UTC),
                end.atOffset(java.time.ZoneOffset.UTC),
                5L));

    List<MetricDto> actual = normalizedMetricService.fetchHistoricalData(from, to, "daily");

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

  @Test
  void fetchResourceMetricsShouldCreateResourceMetricsGroupList() {
    // arrange

    UUID resource1Uuid = UUID.fromString("0356dda1-c0f8-442b-8c56-558925283af6");
    UUID resource2Uuid = UUID.fromString("fccc77e8-26f2-40a1-86c4-2f8756b08333");

    when(metricMapper.toDisplayName("CPUUtilization")).thenReturn("CPU Utilization");
    when(metricMapper.toDisplayName("MemoryUtilization")).thenReturn("Memory Utilization");
    when(metricMapper.toDisplayName("NetworkIn")).thenReturn("Network In");

    when(normalizedMetricsRepository.findDistinctResourceMetrics())
        .thenReturn(
            List.of(
                new ResourceMetricEntry(resource1Uuid, "cpu", "CPUUtilization"),
                new ResourceMetricEntry(resource1Uuid, "memory", "MemoryUtilization"),
                new ResourceMetricEntry(resource2Uuid, "network", "NetworkIn")));

    List<ResourceMetricsGroupDto> expected =
        List.of(
            new ResourceMetricsGroupDto(
                resource1Uuid,
                List.of(
                    new ResourceMetric("CPU Utilization", "cpu"),
                    new ResourceMetric("Memory Utilization", "memory"))),
            new ResourceMetricsGroupDto(
                resource2Uuid, List.of(new ResourceMetric("Network In", "network"))));

    // act
    List<ResourceMetricsGroupDto> actual = normalizedMetricService.fetchResourceMetrics();

    // assert
    assertEquals(expected.size(), actual.size());
    assertEquals(
        expected.stream().collect(Collectors.toSet()), actual.stream().collect(Collectors.toSet()));
  }

  @Test
  void normalizedMetricsShouldReturnEmptyListWhenNoResources() {
    when(normalizedMetricsRepository.findDistinctResourceMetrics()).thenReturn(List.of());

    List<ResourceMetricsGroupDto> actual = normalizedMetricService.fetchResourceMetrics();

    assertEquals(0, actual.size());
    assertTrue(actual.isEmpty());
  }
}
