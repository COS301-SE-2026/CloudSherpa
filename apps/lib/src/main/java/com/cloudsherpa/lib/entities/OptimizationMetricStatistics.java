package com.cloudsherpa.lib.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "optimization_metric_statistics")
public class OptimizationMetricStatistics {
    @Id
    @Column(name = "statistics_id", nullable = false, updatable = false)
    private UUID statisticsId;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "provider", nullable = false, columnDefinition = "public.provider_enum")
    private ProviderEnum provider;

    @Column(name = "metric_name", nullable = false, length = 255)
    private String metricName;

    @Column(name = "window_num_days", nullable = false)
    private Integer windowNumDays;

    @Column(name = "minimum_value")
    private BigDecimal minimumValue;

    @Column(name = "maximum_value")
    private BigDecimal maximumValue;

    @Column(name = "average_value")
    private BigDecimal averageValue;

    @Column(name = "median_value")
    private BigDecimal medianValue;

    @Column(name = "p95_value")
    private BigDecimal p95Value;

    @Column(name = "p99_value")
    private BigDecimal p99Value;

    @Column(name = "standard_deviation")
    private BigDecimal standardDeviation;

    @Column(name = "spike_count")
    private Integer spikeCount;

    @Column(name = "peak_duration_seconds")
    private Integer peakDurationSeconds;

    @Column(name = "completeness_ratio")
    private BigDecimal completenessRatio;

    @Column(name = "window_start", nullable = false)
    private OffsetDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private OffsetDateTime windowEnd;

    @Column(name = "calculated_at")
    private OffsetDateTime calculatedAt;

    protected OptimizationMetricStatistics() {}

    public OptimizationMetricStatistics(
    UUID statisticsId,
    UUID resourceId,
    ProviderEnum provider,
    String metricName,
    Integer windowNumDays,
    BigDecimal minimumValue,
    BigDecimal maximumValue,
    BigDecimal averageValue,
    BigDecimal medianValue,
    BigDecimal p95Value,
    BigDecimal p99Value,
    BigDecimal standardDeviation,
    Integer spikeCount,
    Integer peakDurationSeconds,
    BigDecimal completenessRatio,
    OffsetDateTime windowStart,
    OffsetDateTime windowEnd,
    OffsetDateTime calculatedAt) {
        this.statisticsId = statisticsId;
        this.resourceId = resourceId;
        this.provider = provider;
        this.metricName = metricName;
        this.windowNumDays = windowNumDays;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.averageValue = averageValue;
        this.medianValue = medianValue;
        this.p95Value = p95Value;
        this.p99Value = p99Value;
        this.standardDeviation = standardDeviation;
        this.spikeCount = spikeCount;
        this.peakDurationSeconds = peakDurationSeconds;
        this.completenessRatio = completenessRatio;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.calculatedAt = calculatedAt;
    }

    public UUID getStatisticsId() {
        return statisticsId;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public ProviderEnum getProvider() {
        return provider;
    }

    public String getMetricName() {
        return metricName;
    }

    public Integer getWindowNumDays() {
        return windowNumDays;
    }

    public BigDecimal getMinimumValue() {
        return minimumValue;
    }

    public BigDecimal getMaximumValue() {
        return maximumValue;
    }

    public BigDecimal getAverageValue() {
        return averageValue;
    }

    public BigDecimal getMedianValue() {
        return medianValue;
    }

    public BigDecimal getP95Value() {
        return p95Value;
    }

    public BigDecimal getP99Value() {
        return p99Value;
    }

    public BigDecimal getStandardDeviation() {
        return standardDeviation;
    }

    public Integer getSpikeCount() {
        return spikeCount;
    }

    public Integer getPeakDurationSeconds() {
        return peakDurationSeconds;
    }

    public BigDecimal getCompletenessRatio() {
        return completenessRatio;
    }

    public OffsetDateTime getWindowStart() {
        return windowStart;
    }

    public OffsetDateTime getWindowEnd() {
        return windowEnd;
    }

    public OffsetDateTime getCalculatedAt() {
        return calculatedAt;
    }
}