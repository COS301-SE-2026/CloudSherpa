package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.provider.aws.services.s3.AwsS3;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsCurContext {

  Logger logger = LoggerFactory.getLogger(AwsCurContext.class);

  @JsonIgnore private final AwsS3 s3;

  private String bucketName;
  private String exportPrefix;
  private String exportName;
  private UUID accountId;
  private List<String> processedExports;
  private final String configId;
  private final String userId;

  @JsonIgnore private List<BillingExport> processingExports;
  @JsonIgnore private CloudCredentials credentials;

  private List<String> dataFiles;
  private Path awsCurTmpDir;

  public AwsCurContext(String userId, String configId) {
    this.s3 = new AwsS3();
    this.configId = configId;
    this.userId = userId;
    this.processedExports = new ArrayList<>();
    this.processingExports = new ArrayList<>();
    this.dataFiles = new ArrayList<>();
  }

  public AwsS3 getS3() {
    return s3;
  }

  public CloudCredentials getCredentials() {
    return credentials;
  }

  public void setCredentials(CloudCredentials credentials) {
    this.credentials = credentials;
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

  public UUID getAccountId() {
    return accountId;
  }

  public void setAccountId(UUID accountId) {
    this.accountId = accountId;
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

  public void addProcessedExport(String processedExport) {
    this.processedExports.add(processedExport);
  }

  public List<BillingExport> getProcessingExports() {
    return processingExports;
  }

  public void setProcessingExports(List<BillingExport> processingExports) {
    this.processingExports = processingExports;
  }

  public Path getAwsCurTmpDir() {
    return awsCurTmpDir;
  }

  public String getConfigId() {
    return configId;
  }

  public String getUserId() {
    return userId;
  }

  public UUID getUserUuid() {
    return UUID.fromString(userId);
  }

  public void setAwsCurTmpDir(Path awsCurTmpDir) {
    try {
      Files.createDirectories(awsCurTmpDir);
      this.awsCurTmpDir = awsCurTmpDir;
    } catch (IOException ioException) {
      logger.error("Failed to create temp dir {}", awsCurTmpDir, ioException);
      throw new UncheckedIOException("Failed to set AWS CUR download directory", ioException);
    }
  }
}
