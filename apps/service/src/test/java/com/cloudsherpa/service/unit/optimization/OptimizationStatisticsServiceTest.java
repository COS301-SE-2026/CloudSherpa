package com.cloudsherpa.service.unit.optimization;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.OptimizationMetricStatistics;
import com.cloudsherpa.lib.projections.OptimizationStatisticsAggregate;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import com.cloudsherpa.lib.repositories.OptimizationMetricStatisticsRepository;
import com.cloudsherpa.service.metrics.MetricDisplayNameMapper;
import com.cloudsherpa.service.optimization.service.OptimizationStatisticsService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OptimizationStatisticsServiceTest {

  @Mock private OptimizationMetricStatisticsRepository statisticsRepository;
  @Mock private NormalizedMetricsRepository normalizedMetricsRepository;
  @Mock private MetricDisplayNameMapper metricDisplayNameMapper;

  @InjectMocks private OptimizationStatisticsService service;

  @Captor private ArgumentCaptor<List<OptimizationMetricStatistics>> statsCaptor;

  @Test
  void testGetStatisticsForWindow_InvalidWindow_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> service.getStatisticsForWindow(14));
  }

  @Test
  void testRecalculateStatistics_CreatesNewAndUpdatesExisting() {
    OffsetDateTime end = OffsetDateTime.now(ZoneOffset.UTC);
    int window = 4;

    OptimizationStatisticsAggregate agg1 = mock(OptimizationStatisticsAggregate.class);
    when(agg1.getMetricName()).thenReturn("cpu_raw");
    when(agg1.getResourceId()).thenReturn(UUID.randomUUID());

    when(metricDisplayNameMapper.toDisplayName("cpu_raw")).thenReturn("cpu_utilization");
    when(normalizedMetricsRepository.aggregateStatistics(any(), any())).thenReturn(List.of(agg1));

    OptimizationMetricStatistics existingStat = new OptimizationMetricStatistics();
    existingStat.setStatisticsId(UUID.randomUUID());

    when(statisticsRepository
            .findByResourceIdAndMetricNameAndWindowNumDaysAndWindowStartAndWindowEnd(
                any(), anyString(), anyInt(), any(), any()))
        .thenReturn(Optional.of(existingStat));

    service.recalculateStatistics(end, window);

    verify(statisticsRepository, times(1)).saveAll(statsCaptor.capture());
    List<OptimizationMetricStatistics> saved = statsCaptor.getValue();

    assert (saved.get(0).getStatisticsId().equals(existingStat.getStatisticsId()));
  }
}
