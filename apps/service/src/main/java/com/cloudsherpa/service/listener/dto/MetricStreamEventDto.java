package com.cloudsherpa.service.listener.dto;

import com.cloudsherpa.service.metrics.MetricDisplayNameMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MetricStreamEventDto(
    @JsonProperty("metric_id") UUID metricId,
    @JsonProperty("currency") String currency,
    @JsonProperty("resource_id") UUID resourceId,
    @JsonProperty("metric_type") String metricType,
    @JsonProperty("metric_name") String metricName,
    @JsonProperty("metric_value") BigDecimal metricValue,
    @JsonProperty("period_start") OffsetDateTime periodStart,
    @JsonProperty("period_end") OffsetDateTime periodEnd,
    @JsonProperty("recorded_at") OffsetDateTime recordedAt,
    @JsonProperty("unit") String unit) {
  public MetricStreamEventDto withDisplayNameMappedMetric(MetricDisplayNameMapper mapper) {
    return new MetricStreamEventDto(
        metricId,
        currency,
        resourceId,
        metricType,
        mapper.toDisplayName(metricName),
        metricValue,
        periodStart,
        periodEnd,
        recordedAt,
        unit);
  }
}
