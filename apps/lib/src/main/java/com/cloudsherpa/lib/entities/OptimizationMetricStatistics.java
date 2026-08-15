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

    private OptimizationMetricStatistics(Builder builder) {
        this.statisticsId = builder.statisticsId;
        this.resourceId = builder.resourceId;
        this.provider = builder.provider;
        this.metricName = builder.metricName;
        this.windowNumDays = builder.windowNumDays;
        this.minimumValue = builder.minimumValue;
        this.maximumValue = builder.maximumValue;
        this.averageValue = builder.averageValue;
        this.medianValue = builder.medianValue;
        this.p95Value = builder.p95Value;
        this.p99Value = builder.p99Value;
        this.standardDeviation = builder.standardDeviation;
        this.spikeCount = builder.spikeCount;
        this.peakDurationSeconds = builder.peakDurationSeconds;
        this.completenessRatio = builder.completenessRatio;
        this.windowStart = builder.windowStart;
        this.windowEnd = builder.windowEnd;
        this.calculatedAt = builder.calculatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID statisticsId;
        private UUID resourceId;
        private ProviderEnum provider;
        private String metricName;
        private Integer windowNumDays;
        private BigDecimal minimumValue;
        private BigDecimal maximumValue;
        private BigDecimal averageValue;
        private BigDecimal medianValue;
        private BigDecimal p95Value;
        private BigDecimal p99Value;
        private BigDecimal standardDeviation;
        private Integer spikeCount;
        private Integer peakDurationSeconds;
        private BigDecimal completenessRatio;
        private OffsetDateTime windowStart;
        private OffsetDateTime windowEnd;
        private OffsetDateTime calculatedAt;

        public Builder statisticsId(UUID statisticsId) {
            this.statisticsId = statisticsId;
            return this;
        }

        public Builder resourceId(UUID resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder provider(ProviderEnum provider) {
            this.provider = provider;
            return this;
        }

        public Builder metricName(String metricName) {
            this.metricName = metricName;
            return this;
        }

        public Builder windowNumDays(Integer windowNumDays) {
            this.windowNumDays = windowNumDays;
            return this;
        }

        public Builder minimumValue(BigDecimal minimumValue) {
            this.minimumValue = minimumValue;
            return this;
        }

        public Builder maximumValue(BigDecimal maximumValue) {
            this.maximumValue = maximumValue;
            return this;
        }

        public Builder averageValue(BigDecimal averageValue) {
            this.averageValue = averageValue;
            return this;
        }

        public Builder medianValue(BigDecimal medianValue) {
            this.medianValue = medianValue;
            return this;
        }

        public Builder p95Value(BigDecimal p95Value) {
            this.p95Value = p95Value;
            return this;
        }

        public Builder p99Value(BigDecimal p99Value) {
            this.p99Value = p99Value;
            return this;
        }

        public Builder standardDeviation(BigDecimal standardDeviation) {
            this.standardDeviation = standardDeviation;
            return this;
        }

        public Builder spikeCount(Integer spikeCount) {
            this.spikeCount = spikeCount;
            return this;
        }

        public Builder peakDurationSeconds(Integer peakDurationSeconds) {
            this.peakDurationSeconds = peakDurationSeconds;
            return this;
        }

        public Builder completenessRatio(BigDecimal completenessRatio) {
            this.completenessRatio = completenessRatio;
            return this;
        }

        public Builder windowStart(OffsetDateTime windowStart) {
            this.windowStart = windowStart;
            return this;
        }

        public Builder windowEnd(OffsetDateTime windowEnd) {
            this.windowEnd = windowEnd;
            return this;
        }

        public Builder calculatedAt(OffsetDateTime calculatedAt) {
            this.calculatedAt = calculatedAt;
            return this;
        }

        public OptimizationMetricStatistics build() {
            return new OptimizationMetricStatistics(this);
        }
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