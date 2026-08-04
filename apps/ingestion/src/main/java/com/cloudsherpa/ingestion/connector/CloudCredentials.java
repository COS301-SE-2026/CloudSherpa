package com.cloudsherpa.ingestion.connector;

public class CloudCredentials {

  // AWS
  private String accessKeyId;
  private String secretAccessKey;

  // Azure
  private String tenantId;
  private String clientId;
  private String clientSecret;

  // GCP
  private String projectId;
  private String serviceAccountJson;

  public String getAccessKey() {
    return accessKeyId;
  }

  public void setAccessKey(String accessKey) {
    this.accessKeyId = accessKey;
  }

  public String getSecretKey() {
    return secretAccessKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretAccessKey = secretKey;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getServiceAccountJson() {
    return serviceAccountJson;
  }

  public void setServiceAccountJson(String serviceAccountJson) {
    this.serviceAccountJson = serviceAccountJson;
  }
}
