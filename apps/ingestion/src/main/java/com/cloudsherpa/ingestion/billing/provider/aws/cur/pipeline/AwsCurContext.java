package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import com.cloudsherpa.ingestion.provider.aws.services.s3.AwsS3;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsCurContext {

  Logger logger = LoggerFactory.getLogger(AwsCurContext.class);

  // Runtime dependency
  @JsonIgnore private final AwsS3 s3;

  private String bucketName;
  private String exportPrefix;
  private String exportName;
  private List<String> processedExports;

  // Internal working state while the pipeline is running
  @JsonIgnore private List<AwsCurExport> processingExports;

  private List<String> dataFiles;
  private Path awsCurTmpDir;

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

  public Path getAwsCurTmpDir() {
    return awsCurTmpDir;
  }

  public void setAwsCurTmpDir(Path awsCurTmpDir) {
    try {
      Files.createDirectories(awsCurTmpDir);
      this.awsCurTmpDir = awsCurTmpDir;
    } catch (IOException ioException) {
      logger.error("Failed to create temp dir {}", awsCurTmpDir, ioException);
      throw new RuntimeException("Failed to set AWS CUR download directory");
    }
  }
}
