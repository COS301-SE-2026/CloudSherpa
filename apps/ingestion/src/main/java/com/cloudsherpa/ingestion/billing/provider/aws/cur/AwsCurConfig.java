package com.cloudsherpa.ingestion.billing.provider.aws.cur;

public class AwsCurConfig {
  private final String bucketName;
  private final String exportPrefix;
  private final String exportName;

  public AwsCurConfig(String bucketName, String exportPrefix, String exportName) {
    this.bucketName = bucketName;
    this.exportPrefix = exportPrefix;
    this.exportName = exportName;
  }

  public String getBucketName() {
    return bucketName;
  }

  public String getExportPrefix() {
    return exportPrefix;
  }

  public String getExportName() {
    return exportName;
  }

  public String getTotalPrefix() {
    return exportPrefix + "/" + exportName;
  }
}
