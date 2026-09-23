package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "billing_forecast_runs")
public class BillingForecastRun {

  @Id
  @Column(name = "forecast_run_id", nullable = false)
  private UUID forecastRunId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "forecast_execution_status", nullable = false,
      columnDefinition = "public.billing_forecast_execution_status")
  private ForecastExecutionStatusEnum forecastExecutionStatus;

  @Column(name = "forecast_execution_log_timestamp", nullable = false)
  private Instant timestamp;

  protected BillingForecastRun() {}

  public BillingForecastRun(
      UUID forecastRunId, ForecastExecutionStatusEnum forecastExecutionStatus, Instant timestamp) {
    this.forecastRunId = forecastRunId;
    this.forecastExecutionStatus = forecastExecutionStatus;
    this.timestamp = timestamp;
  }

  public UUID getForecastRunId() {
    return forecastRunId;
  }

  public ForecastExecutionStatusEnum getForecastExecutionStatus() {
    return forecastExecutionStatus;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setForecastExecutionStatus(ForecastExecutionStatusEnum forecastExecutionStatus) {
    this.forecastExecutionStatus = forecastExecutionStatus;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }
}
