package com.cloudsherpa.ingestion.connector;

public class AccountScope {

  private String accountId; // AWS
  private String subscriptionId; // Azure
  private String projectId; // GCP

  private String billingAccountId;

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getSubscriptionId() {
    return subscriptionId;
  }

  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getBillingAccountId() {
    return billingAccountId;
  }

  public void setBillingAccountId(String billingAccountId) {
    this.billingAccountId = billingAccountId;
  }
}
