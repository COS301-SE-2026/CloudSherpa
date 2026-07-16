package com.cloudsherpa.ingestion.billing.provider.aws.cur.serialization.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ManifestConfig {
  private String executionId;
  private List<String> dataFiles;

  public ManifestConfig() {
    // Required by Jackson
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public List<String> getDataFiles() {
    return dataFiles;
  }

  public void setDataFiles(List<String> dataFiles) {
    this.dataFiles = dataFiles;
  }
}
