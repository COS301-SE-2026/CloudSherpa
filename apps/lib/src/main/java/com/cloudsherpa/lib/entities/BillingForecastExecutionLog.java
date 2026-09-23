package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "billing_forecast_execution_logs")
public class BillingForecastExecutionLog {

  @Id
  @Column(name = "log_id")
  private UUID logId;

  @ManyToOne
  @JoinColumn(name = "execution_id", insertable = false, updatable = false)
  private BillingExportExecution execution;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.ENUM)
  @Column(name = "forecast_execution_status", nullable = false)
  private ForecastExecutionStatusEnum forecastExecutionStatus;

  @Column(name = "forecast_execution_log_timestamp", nullable = false)
  private Instant timestamp;

  protected BillingForecastExecutionLog() {}

  public BillingForecastExecutionLog(
      UUID logId,
      BillingExportExecution execution,
      ForecastExecutionStatusEnum forecastExecutionStatus,
      Instant timestamp) {
    this.logId = logId;
    this.execution = execution;
    this.forecastExecutionStatus = forecastExecutionStatus;
    this.timestamp = timestamp;
  }

  public UUID getLogId() {
    return logId;
  }

  public BillingExportExecution getExecution() {
    return execution;
  }

  public ForecastExecutionStatusEnum getForecastExecutionStatus() {
    return forecastExecutionStatus;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setLogId(UUID logId) {
    this.logId = logId;
  }

  public void setExecution(BillingExportExecution execution) {
    this.execution = execution;
  }

  public void setForecastExecutionStatus(ForecastExecutionStatusEnum forecastExecutionStatus) {
    this.forecastExecutionStatus = forecastExecutionStatus;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }
}
