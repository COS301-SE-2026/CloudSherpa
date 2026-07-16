package com.cloudsherpa.ingestion.billing.provider.aws.cur.serialization.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ManifestConfig {
  private List<String> dataFiles;

  public ManifestConfig() {
    // Required by Jackson
  }

  public List<String> getDataFiles() {
    return dataFiles;
  }

  public void setDataFiles(List<String> dataFiles) {
    this.dataFiles = dataFiles;
  }
}
