package com.cloudsherpa.ingestion.connector;

// Jackson config for deserializing AWS Credentials
public class AwsCredentials {
  private String accessKeyId;
  private String secretAccessKey;

  public AwsCredentials() {
    // Required by jackson
  }

  public String getAccessKeyId() {
    return accessKeyId;
  }

  public void setAccessKeyId(String accessKeyId) {
    this.accessKeyId = accessKeyId;
  }

  public String getSecretAccessKey() {
    return secretAccessKey;
  }

  public void setSecretAccessKey(String secretAccessKey) {
    this.secretAccessKey = secretAccessKey;
  }
}
