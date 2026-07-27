package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "billing_export_execution", schema = "public")
public class BillingExportExecution {

  @Id
  @Column(name = "execution_id", nullable = false)
  private UUID id;

  @Column(name = "config_id", nullable = false)
  private UUID configId;

  @ManyToOne
  @JoinColumn(name = "config_id", nullable = false, insertable = false, updatable = false)
  private BillingExportConfig config;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", nullable = false, columnDefinition = "public.execution_status_enum")
  private ExecutionStatusEnum status;

  @Column(name = "rows_processed")
  private Integer rowsProcessed;

  @Column(name = "started_at")
  private OffsetDateTime startedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "error_message", columnDefinition = "text")
  private String errorMessage;

  protected BillingExportExecution() {
  }

  public BillingExportExecution(
      UUID id,
      UUID configId,
      ExecutionStatusEnum status,
      Integer rowsProcessed,
      OffsetDateTime startedAt,
      OffsetDateTime completedAt,
      String errorMessage) {
    this.id = id;
    this.configId = configId;
    this.status = status;
    this.rowsProcessed = rowsProcessed;
    this.startedAt = startedAt;
    this.completedAt = completedAt;
    this.errorMessage = errorMessage;
  }

  public BillingExportExecution(
      UUID id,
      UUID configId,
      ExecutionStatusEnum status) {
    this.id = id;
    this.configId = configId;
    this.status = status;
  }

  public UUID getId() {
    return id;
  }

  public UUID getConfigId() {
    return configId;
  }

  public BillingExportConfig getConfig() {
    return config;
  }

  public ExecutionStatusEnum getStatus() {
    return status;
  }

  public Integer getRowsProcessed() {
    return rowsProcessed;
  }

  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setStatus(ExecutionStatusEnum executionStatus) {
    this.status = executionStatus;
  }

  public void setRowsProcessed(Integer rowsProcessed) {
    this.rowsProcessed = rowsProcessed;
  }

  public void setStartedAt(OffsetDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public void setCompletedAt(OffsetDateTime completedAt) {
    this.completedAt = completedAt;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

}