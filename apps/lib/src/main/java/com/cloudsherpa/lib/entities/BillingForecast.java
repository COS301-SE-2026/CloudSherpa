package com.cloudsherpa.lib.entities;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "billing_forecasts")
public class BillingForecast {

  @Id
  @Column(name = "forecast_id")
  private UUID forecastId;

  @Column(name = "forecast_run_id", nullable = false)
  private UUID forecastRunId;

  @ManyToOne
  @JoinColumn(name = "forecast_run_id", insertable = false, updatable = false)
  private BillingForecastRun forecastRun;

  @Column(name = "forecast_timestamp", nullable = false)
  private OffsetDateTime forecastTimestamp;

  @Column(name = "forecast_window", nullable = false)
  private Integer forecastWindow;

  @Column(name = "cumulative_forecast_value", nullable = false)
  private BigDecimal cumulativeForecastValue;

  @Column(name = "cumulative_past_forecast_value", nullable = false)
  private BigDecimal cumulativePastForecastValue;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "forecast_series", columnDefinition = "jsonb", nullable = false)
  private JsonNode forecastSeries;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "failed_charges", columnDefinition = "text[]", nullable = false)
  private List<String> failedCharges;

  @Column(name = "past_variance", nullable = false)
  private BigDecimal pastVariance;

  @Column(name = "daily_burn_rate", nullable = false)
  private BigDecimal dailyBurnRate;

  @Column(name = "highest_cost_driver", nullable = false)
  private String highestCostDriver;

  @Column(name = "highest_cost_acceleration", nullable = false)
  private String highestCostAcceleration;

  @Column(name = "acceleration_rate")
  private BigDecimal accelerationRate;

  protected BillingForecast() {}

  private BillingForecast(Builder builder) {
    this.forecastId = builder.forecastId;
    this.forecastRunId = builder.forecastRunId;
    this.forecastRun = builder.forecastRun;
    this.forecastTimestamp = builder.forecastTimestamp;
    this.forecastWindow = builder.forecastWindow;
    this.cumulativeForecastValue = builder.cumulativeForecastValue;
    this.cumulativePastForecastValue = builder.cumulativePastForecastValue;
    this.forecastSeries = builder.forecastSeries;
    this.failedCharges = builder.failedCharges;
    this.pastVariance = builder.pastVariance;
    this.dailyBurnRate = builder.dailyBurnRate;
    this.highestCostDriver = builder.highestCostDriver;
    this.highestCostAcceleration = builder.highestCostAcceleration;
    this.accelerationRate = builder.accelerationRate;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID forecastId;
    private UUID forecastRunId;
    private BillingForecastRun forecastRun;
    private OffsetDateTime forecastTimestamp;
    private Integer forecastWindow;
    private BigDecimal cumulativeForecastValue;
    private BigDecimal cumulativePastForecastValue;
    private JsonNode forecastSeries;
    private List<String> failedCharges;
    private BigDecimal pastVariance;
    private BigDecimal dailyBurnRate;
    private String highestCostDriver;
    private String highestCostAcceleration;
    private BigDecimal accelerationRate;

    public Builder forecastId(UUID forecastId) {
      this.forecastId = forecastId;
      return this;
    }

    public Builder forecastRunId(UUID forecastRunId) {
      this.forecastRunId = forecastRunId;
      return this;
    }

    public Builder forecastRun(BillingForecastRun forecastRun) {
      this.forecastRun = forecastRun;
      return this;
    }

    public Builder forecastTimestamp(OffsetDateTime forecastTimestamp) {
      this.forecastTimestamp = forecastTimestamp;
      return this;
    }

    public Builder forecastWindow(Integer forecastWindow) {
      this.forecastWindow = forecastWindow;
      return this;
    }

    public Builder cumulativeForecastValue(BigDecimal cumulativeForecastValue) {
      this.cumulativeForecastValue = cumulativeForecastValue;
      return this;
    }

    public Builder cumulativePastForecastValue(BigDecimal cumulativePastForecastValue) {
      this.cumulativePastForecastValue = cumulativePastForecastValue;
      return this;
    }

    public Builder forecastSeries(JsonNode forecastSeries) {
      this.forecastSeries = forecastSeries;
      return this;
    }

    public Builder failedCharges(List<String> failedCharges) {
      this.failedCharges = failedCharges;
      return this;
    }

    public Builder pastVariance(BigDecimal pastVariance) {
      this.pastVariance = pastVariance;
      return this;
    }

    public Builder dailyBurnRate(BigDecimal dailyBurnRate) {
      this.dailyBurnRate = dailyBurnRate;
      return this;
    }

    public Builder highestCostDriver(String highestCostDriver) {
      this.highestCostDriver = highestCostDriver;
      return this;
    }

    public Builder highestCostAcceleration(String highestCostAcceleration) {
      this.highestCostAcceleration = highestCostAcceleration;
      return this;
    }

    public Builder accelerationRate(BigDecimal accelerationRate) {
      this.accelerationRate = accelerationRate;
      return this;
    }

    public BillingForecast build() {
      return new BillingForecast(this);
    }
  }

  public UUID getForecastId() {
    return forecastId;
  }

  public UUID getForecastRunId() {
    return forecastRunId;
  }

  public BillingForecastRun getForecastRun() {
    return forecastRun;
  }

  public OffsetDateTime getForecastTimestamp() {
    return forecastTimestamp;
  }

  public Integer getForecastWindow() {
    return forecastWindow;
  }

  public BigDecimal getCumulativeForecastValue() {
    return cumulativeForecastValue;
  }

  public BigDecimal getCumulativePastForecastValue() {
    return cumulativePastForecastValue;
  }

  public JsonNode getForecastSeries() {
    return forecastSeries;
  }

  public List<String> getFailedCharges() {
    return failedCharges;
  }

  public BigDecimal getPastVariance() {
    return pastVariance;
  }

  public BigDecimal getDailyBurnRate() {
    return dailyBurnRate;
  }

  public String getHighestCostDriver() {
    return highestCostDriver;
  }

  public String getHighestCostAcceleration() {
    return highestCostAcceleration;
  }

  public BigDecimal getAccelerationRate() {
    return accelerationRate;
  }

  public void setForecastRunId(UUID forecastRunId) {
    this.forecastRunId = forecastRunId;
  }

  public void setForecastRun(BillingForecastRun forecastRun) {
    this.forecastRun = forecastRun;
  }

  public void setForecastTimestamp(OffsetDateTime forecastTimestamp) {
    this.forecastTimestamp = forecastTimestamp;
  }

  public void setForecastWindow(Integer forecastWindow) {
    this.forecastWindow = forecastWindow;
  }

  public void setCumulativeForecastValue(BigDecimal cumulativeForecastValue) {
    this.cumulativeForecastValue = cumulativeForecastValue;
  }

  public void setCumulativePastForecastValue(BigDecimal cumulativePastForecastValue) {
    this.cumulativePastForecastValue = cumulativePastForecastValue;
  }

  public void setForecastSeries(JsonNode forecastSeries) {
    this.forecastSeries = forecastSeries;
  }

  public void setFailedCharges(List<String> failedCharges) {
    this.failedCharges = failedCharges;
  }

  public void setPastVariance(BigDecimal pastVariance) {
    this.pastVariance = pastVariance;
  }

  public void setDailyBurnRate(BigDecimal dailyBurnRate) {
    this.dailyBurnRate = dailyBurnRate;
  }

  public void setHighestCostDriver(String highestCostDriver) {
    this.highestCostDriver = highestCostDriver;
  }

  public void setHighestCostAcceleration(String highestCostAcceleration) {
    this.highestCostAcceleration = highestCostAcceleration;
  }

  public void setAccelerationRate(BigDecimal accelerationRate) {
    this.accelerationRate = accelerationRate;
  }
}
