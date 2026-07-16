package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import java.util.List;

public class AwsCurExport {
  private String exportId;
  private List<String> dataFiles;

  public AwsCurExport(String exportId, List<String> dataFiles) {
    this.exportId = exportId;
    this.dataFiles = dataFiles;
  }

  public String getExportId() {
    return exportId;
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
}
