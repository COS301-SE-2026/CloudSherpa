package com.cloudsherpa.service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloudsherpa.service.analytics.entities.NormalizedMetrics;
import com.cloudsherpa.service.analytics.projections.ResourceNames;
import com.cloudsherpa.service.analytics.repository.NormalizedMetricsRepository;
import com.cloudsherpa.service.analytics.repository.ResourceRepository;
import com.cloudsherpa.service.analytics.service.NormalizedMetricService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NormalizedMetricServiceTest {

  @Mock private NormalizedMetricsRepository normalizedMetricsRepository;

  @Mock private ResourceRepository resourceRepository;

  @InjectMocks private NormalizedMetricService normalizedMetricService;

  @Test
  void fetchHistoricalDataReturnsMetricsBetweenDates() throws Exception {
    String from = "2026-04-01T00:00:00Z";
    String to = "2026-04-30T00:00:00Z";

    OffsetDateTime parsedFrom = OffsetDateTime.parse(from);
    OffsetDateTime parsedTo = OffsetDateTime.parse(to);

    List<NormalizedMetrics> expected = List.of(new NormalizedMetrics());

    when(normalizedMetricsRepository.findByPeriodStartBetween(parsedFrom, parsedTo))
        .thenReturn(expected);

    List<NormalizedMetrics> actual = normalizedMetricService.fetchHistoricalData(from, to);

    assertEquals(expected, actual);
  }

  @Test
  void throwExceptionWhenFromIsAfterTo() {
    String from = "2026-04-15T00:00:00Z";
    String to = "2026-04-05T00:00:00Z";

    Exception exception =
        assertThrows(Exception.class, () -> normalizedMetricService.fetchHistoricalData(from, to));

    assertEquals("Invalid interval", exception.getMessage());
  }

  @Test
  void throwsExceptionWhenFromNotIsoDate() {
    String from = "2026-04-15T00:00:0";
    String to = "2026-04-05T00:00:00Z";

    Exception exception =
        assertThrows(Exception.class, () -> normalizedMetricService.fetchHistoricalData(from, to));

    assertEquals("Date Strings do not conform to ISO-8601 standard", exception.getMessage());
  }

  @Test
  void throwsExceptionWhenToNotIsoDate() {
    String from = "2026-04-15T00:00:00Z";
    String to = "2026-04-05T00::00Z";

    Exception exception =
        assertThrows(Exception.class, () -> normalizedMetricService.fetchHistoricalData(from, to));

    assertEquals("Date Strings do not conform to ISO-8601 standard", exception.getMessage());
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
  void httpNoContentWhenNoResourceNames() {

    when(resourceRepository.findResourceNames()).thenReturn(List.of());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> normalizedMetricService.fetchResourceNames());

    assertEquals("No resources found", exception.getReason());
  }
}
