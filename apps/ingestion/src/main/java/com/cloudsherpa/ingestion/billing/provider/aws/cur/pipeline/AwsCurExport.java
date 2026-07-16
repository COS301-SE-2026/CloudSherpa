package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AwsCurExport {
  private String exportId;
  private List<String> dataFiles;
  private String encoding;
  private List<Path> tmpPaths;

  public AwsCurExport(String exportId, List<String> dataFiles) {
    this.exportId = exportId;
    this.dataFiles = dataFiles;
    this.tmpPaths = new ArrayList<>();
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
}
