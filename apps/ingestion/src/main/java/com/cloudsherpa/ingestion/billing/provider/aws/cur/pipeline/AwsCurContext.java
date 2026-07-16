package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import com.cloudsherpa.ingestion.provider.aws.services.s3.AwsS3;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

public class AwsCurContext {
  // Runtime dependency; should never be included in an API response
  @JsonIgnore private final AwsS3 s3;

  private String bucketName;
  private String exportPrefix;
  private String exportName;
  private List<String> processedExports;

  // Internal working state while the pipeline is running
  @JsonIgnore private List<AwsCurExport> processingExports;

  private List<String> dataFiles;

  public AwsCurContext() {
    this.s3 = new AwsS3();
    this.processedExports = new ArrayList<>();
    this.processingExports = new ArrayList<>();
    this.dataFiles = new ArrayList<>();
  }

  public AwsS3 getS3() {
    return s3;
  }

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getExportPrefix() {
    return exportPrefix;
  }

  public void setExportPrefix(String exportPrefix) {
    this.exportPrefix = exportPrefix;
  }

  public String getExportName() {
    return exportName;
  }

  public void setExportName(String exportName) {
    this.exportName = exportName;
  }

  public List<String> getDataFiles() {
    return dataFiles;
  }

  public void setDataFiles(List<String> dataFiles) {
    this.dataFiles = dataFiles;
  }

  public List<String> getProcessedExports() {
    return processedExports;
  }

  public void setProcessedExports(List<String> processedExports) {
    this.processedExports = processedExports;
  }

  public List<AwsCurExport> getProcessingExports() {
    return processingExports;
  }

  public void setProcessingExports(List<AwsCurExport> processingExports) {
    this.processingExports = processingExports;
  }
}
