package com.cloudsherpa.service.unit.intelligence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.ResourceUsageForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.ResourceUsageForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.SanatizedSeries;
import com.cloudsherpa.service.intelligence.exceptions.InsufficientContextAvailable;
import com.cloudsherpa.service.intelligence.service.Sampler;
import com.cloudsherpa.service.intelligence.service.usage.UsageForecastingService;
import com.cloudsherpa.service.metrics.MetricDisplayNameMapper;
import com.cloudsherpa.service.metrics.ResourceProviderResolver;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UsageForecastingServiceTest {
  private static final UUID RESOURCE_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final String METRIC_TYPE = "CPUUtilization";
  private static final OffsetDateTime FORECAST_HORIZON =
      OffsetDateTime.parse("2026-09-01T00:00:00Z");
  protected static final int CONTEXT_LENGTH = 8092;

  @Mock Sampler sampler;

  @Mock NormalizedMetricsRepository normalizedMetricsRepository;

  @Mock RestClient restClient;

  @Mock RestClient.RequestBodyUriSpec requestBodyUriSpec;

  @Mock RestClient.RequestBodySpec requestBodySpec;

  @Mock RestClient.ResponseSpec responseSpec;

  @Mock ResourceProviderResolver resourceResolver;

  @Mock MetricDisplayNameMapper displayNameMapper;

  private ResourceUsageForecastRequestDto validRequest;

  @InjectMocks private UsageForecastingService usageForecastingService;

  @BeforeEach
  void setUp() {
    this.validRequest =
        new ResourceUsageForecastRequestDto(RESOURCE_ID, METRIC_TYPE, FORECAST_HORIZON);
  }

  @Test
  void shouldThrowNotFoundWhenResourceHasNoMetricData() {
    // Arrange
    when(normalizedMetricsRepository.getTimestampedMetricValues(
            RESOURCE_ID, METRIC_TYPE, PageRequest.of(0, CONTEXT_LENGTH)))
        .thenReturn(List.of());

    mockResourceMetricMapping();

    // Act & Assert
    assertThrows(
        InsufficientContextAvailable.class,
        () -> usageForecastingService.forecastUsage(validRequest));
  }

  @Test
  void shouldThrowWhenInsufficientHistoricalUsageContext() {
    // Arrange
    List<TimestampedNumericDataPoint> usageSeries =
        List.of(
            new TimestampedNumericDataPoint(
                BigDecimal.valueOf(10), Instant.parse("2026-08-01T00:00:00Z")),
            new TimestampedNumericDataPoint(
                BigDecimal.valueOf(12), Instant.parse("2026-08-02T00:00:00Z")));

    when(normalizedMetricsRepository.getTimestampedMetricValues(
            RESOURCE_ID, METRIC_TYPE, PageRequest.of(0, CONTEXT_LENGTH)))
        .thenReturn(usageSeries);
    when(sampler.sample(usageSeries, true)).thenReturn(new SanatizedSeries(usageSeries, 86_400));
    mockResourceMetricMapping();

    // Act & Assert
    assertThrows(
        InsufficientContextAvailable.class,
        () -> usageForecastingService.forecastUsage(validRequest));
  }

  @Test
  void shouldThrowWhenIntelligenceResponseNull() {
    // Arrange
    mockForecastingServiceResponse(null);

    List<TimestampedNumericDataPoint> usageSeries = getValidUsageSeries();

    when(normalizedMetricsRepository.getTimestampedMetricValues(
            RESOURCE_ID, METRIC_TYPE, PageRequest.of(0, CONTEXT_LENGTH)))
        .thenReturn(usageSeries);
    when(sampler.sample(usageSeries, true)).thenReturn(new SanatizedSeries(usageSeries, 86_400));
    mockResourceMetricMapping();

    // Act and Assert
    ResponseStatusException responseStatusException =
        assertThrows(
            ResponseStatusException.class,
            () -> usageForecastingService.forecastUsage(validRequest));
    assertEquals(HttpStatus.BAD_GATEWAY, responseStatusException.getStatusCode());
  }

  @Test
  void shouldCallRepositoryWithContextLimitAndRequestedResourceMetric() {
    List<TimestampedNumericDataPoint> usageSeries = getValidUsageSeries();

    IntelligenceForecastResponseDto forecastResponse = getValidForecastResponse();

    when(normalizedMetricsRepository.getTimestampedMetricValues(
            RESOURCE_ID, METRIC_TYPE, PageRequest.of(0, CONTEXT_LENGTH)))
        .thenReturn(usageSeries);

    when(sampler.sample(usageSeries, true)).thenReturn(new SanatizedSeries(usageSeries, 86_400));
    mockResourceMetricMapping();

    mockForecastingServiceResponse(forecastResponse);

    // Act
    usageForecastingService.forecastUsage(validRequest);

    // Assert
    verify(normalizedMetricsRepository)
        .getTimestampedMetricValues(RESOURCE_ID, METRIC_TYPE, PageRequest.of(0, CONTEXT_LENGTH));
  }

  @Test
  void shouldCallRepositoryAggregateQueryWhenProviderGCP() {
    List<TimestampedNumericDataPoint> usageSeries = getValidUsageSeries();

    IntelligenceForecastResponseDto forecastResponse = getValidForecastResponse();

    when(normalizedMetricsRepository.getAggregatedTimestampedMetricValuesAfterDate(
            RESOURCE_ID, METRIC_TYPE, PageRequest.of(0, CONTEXT_LENGTH)))
        .thenReturn(usageSeries);

    when(sampler.sample(usageSeries, true)).thenReturn(new SanatizedSeries(usageSeries, 86_400));
    mockGcpMetricMapping();

    mockForecastingServiceResponse(forecastResponse);

    // Act
    usageForecastingService.forecastUsage(validRequest);

    // Assert
    verify(normalizedMetricsRepository)
        .getAggregatedTimestampedMetricValuesAfterDate(
            RESOURCE_ID, METRIC_TYPE, PageRequest.of(0, CONTEXT_LENGTH));
  }

  @Test
  void shouldNotCallForecastingServiceWhenHistoricalContextIsInsufficient() {
    // Arrange
    List<TimestampedNumericDataPoint> usageSeries =
        List.of(
            new TimestampedNumericDataPoint(
                BigDecimal.valueOf(10), Instant.parse("2026-08-01T00:00:00Z")),
            new TimestampedNumericDataPoint(
                BigDecimal.valueOf(12), Instant.parse("2026-08-02T00:00:00Z")));

    when(normalizedMetricsRepository.getTimestampedMetricValues(
            RESOURCE_ID, METRIC_TYPE, PageRequest.of(0, CONTEXT_LENGTH)))
        .thenReturn(usageSeries);
    when(sampler.sample(usageSeries, true)).thenReturn(new SanatizedSeries(usageSeries, 86_400));
    mockResourceMetricMapping();

    // Act & Assert
    assertThrows(
        InsufficientContextAvailable.class,
        () -> usageForecastingService.forecastUsage(validRequest));
    verifyNoInteractions(restClient);
  }

  @Test
  void shouldMapIntelligenceForecastResponseFieldsToUsageResponse() {
    // Arrange
    List<TimestampedNumericDataPoint> usageSeries = getValidUsageSeries();

    List<LocalDateTime> timestamps =
        List.of(
            LocalDateTime.parse("2026-08-04T00:00:00"), LocalDateTime.parse("2026-08-05T00:00:00"));
    List<BigDecimal> forecast = List.of(BigDecimal.valueOf(20), BigDecimal.valueOf(21));
    List<BigDecimal> q1 = List.of(BigDecimal.valueOf(18), BigDecimal.valueOf(19));
    List<BigDecimal> q3 = List.of(BigDecimal.valueOf(22), BigDecimal.valueOf(23));

    IntelligenceForecastResponseDto forecastResponse =
        getForecastResponse(forecast, timestamps, q1, q3);

    when(normalizedMetricsRepository.getTimestampedMetricValues(
            RESOURCE_ID, METRIC_TYPE, PageRequest.of(0, CONTEXT_LENGTH)))
        .thenReturn(usageSeries);
    when(sampler.sample(usageSeries, true)).thenReturn(new SanatizedSeries(usageSeries, 86_400));
    mockForecastingServiceResponse(forecastResponse);
    mockResourceMetricMapping();

    // Act
    ResourceUsageForecastResponseDto response = usageForecastingService.forecastUsage(validRequest);

    // Assert
    assertEquals(timestamps, response.horizonTimestamps());
    assertEquals(forecast, response.predictedValues());
    assertEquals(q1, response.q1Values());
    assertEquals(q3, response.q3Values());
  }

  @Test
  void shouldCapForecastAtZero() {
    // Arrange
    List<TimestampedNumericDataPoint> usageSeries = getValidUsageSeries();

    List<LocalDateTime> timestamps =
        List.of(
            LocalDateTime.parse("2026-08-04T00:00:00"), LocalDateTime.parse("2026-08-05T00:00:00"));
    List<BigDecimal> forecast = List.of(BigDecimal.valueOf(-20), BigDecimal.valueOf(21));
    List<BigDecimal> q1 = List.of(BigDecimal.valueOf(18), BigDecimal.valueOf(19));
    List<BigDecimal> q3 = List.of(BigDecimal.valueOf(22), BigDecimal.valueOf(23));

    IntelligenceForecastResponseDto forecastResponse =
        getForecastResponse(forecast, timestamps, q1, q3);

    when(normalizedMetricsRepository.getTimestampedMetricValues(
            RESOURCE_ID, METRIC_TYPE, PageRequest.of(0, CONTEXT_LENGTH)))
        .thenReturn(usageSeries);
    when(sampler.sample(usageSeries, true)).thenReturn(new SanatizedSeries(usageSeries, 86_400));
    mockForecastingServiceResponse(forecastResponse);
    mockResourceMetricMapping();

    List<BigDecimal> expectedForecast = List.of(BigDecimal.ZERO, BigDecimal.valueOf(21));

    // Act
    ResourceUsageForecastResponseDto response = usageForecastingService.forecastUsage(validRequest);

    // Assert
    assertEquals(timestamps, response.horizonTimestamps());
    assertEquals(expectedForecast, response.predictedValues());
    assertEquals(q1, response.q1Values());
    assertEquals(q3, response.q3Values());
  }

  private void mockResourceMetricMapping() {
    when(resourceResolver.resolveProvider(RESOURCE_ID)).thenReturn(ProviderEnum.AWS);
    when(displayNameMapper.toCanonicalName(ProviderEnum.AWS.toString(), METRIC_TYPE))
        .thenReturn(METRIC_TYPE);
  }

  private void mockGcpMetricMapping() {
    when(resourceResolver.resolveProvider(RESOURCE_ID)).thenReturn(ProviderEnum.GCP);
    when(displayNameMapper.toCanonicalName(ProviderEnum.GCP.toString(), METRIC_TYPE))
        .thenReturn(METRIC_TYPE);
  }

  private List<TimestampedNumericDataPoint> getValidUsageSeries() {
    return List.of(
        new TimestampedNumericDataPoint(
            BigDecimal.valueOf(10), Instant.parse("2026-08-01T00:00:00Z")),
        new TimestampedNumericDataPoint(
            BigDecimal.valueOf(12), Instant.parse("2026-08-02T00:00:00Z")),
        new TimestampedNumericDataPoint(
            BigDecimal.valueOf(14), Instant.parse("2026-08-03T00:00:00Z")));
  }

  private IntelligenceForecastResponseDto getValidForecastResponse() {
    return getForecastResponse(
        List.of(BigDecimal.valueOf(20)),
        List.of(LocalDateTime.parse("2026-08-04T00:00:00")),
        List.of(BigDecimal.valueOf(18)),
        List.of(BigDecimal.valueOf(22)));
  }

  private IntelligenceForecastResponseDto getForecastResponse(
      List<BigDecimal> forecast,
      List<LocalDateTime> timestamps,
      List<BigDecimal> q1,
      List<BigDecimal> q3) {
    return new IntelligenceForecastResponseDto(forecast, timestamps, q1, q3);
  }

  private void mockForecastingServiceResponse(IntelligenceForecastResponseDto response) {
    ReflectionTestUtils.setField(usageForecastingService, "intelligenceApiKey", "test-key");

    when(restClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri("/forecast-chronos")).thenReturn(requestBodySpec);
    when(requestBodySpec.header("X-API-Key", "test-key")).thenReturn(requestBodySpec);
    when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
    when(requestBodySpec.body(any(IntelligenceForecastRequestDto.class)))
        .thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(IntelligenceForecastResponseDto.class)).thenReturn(response);
  }
}
