package com.cloudsherpa.ingestion.billing.provider.aws.cur;

import com.cloudsherpa.lib.entities.ExecutionStatusEnum;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AwsCurExport {
  private String exportId;
  private List<String> dataFiles;
  private String encoding;
  private List<Path> tmpPaths;
  private String configId;
  private ExecutionStatusEnum executionStatus;
  private Integer rowsProcessed;
  private OffsetDateTime startedAt;
  private OffsetDateTime completedAt;
  private String errorMessage;

  public AwsCurExport(String exportId, String configId, List<String> dataFiles) {
    this.exportId = exportId;
    this.dataFiles = dataFiles;
    this.tmpPaths = new ArrayList<>();
    this.configId = configId;
    this.executionStatus = ExecutionStatusEnum.pending;
    this.rowsProcessed = 0;
  }

  public String getExportId() {
    return exportId;
  }

  public UUID getUuidExportId() {
    return UUID.fromString(exportId);
  }

  public void setExportId(String exportId) {
    this.exportId = exportId;
  }

  public List<String> getDataFiles() {
    return dataFiles;
  }

  public void setDataFiles(List<String> dataFiles) {
    this.dataFiles = dataFiles;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public List<Path> getTmpPaths() {
    return tmpPaths;
  }

  public void addTmpPath(Path tmpPath) {
    this.tmpPaths.add(tmpPath);
  }

  public ExecutionStatusEnum getExecutionStatus() {
    return executionStatus;
  }

  public void setExecutionStatus(ExecutionStatusEnum executionStatus) {
    this.executionStatus = executionStatus;
  }

  public String getConfigId() {
    return configId;
  }

  public UUID getUuidConfigId() {
    return UUID.fromString(configId);
  }

  public Integer getRowsProcessed() {
    return rowsProcessed;
  }

  public void setRowsProcessed(Integer rowsProcessed) {
    this.rowsProcessed = rowsProcessed;
  }

  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(OffsetDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(OffsetDateTime completedAt) {
    this.completedAt = completedAt;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
