package com.cloudsherpa.ingestion.connector;

import java.util.List;

public class AccountScope {
  private String provider;
  private String accountId; // AWS
  private String subscriptionId; // Azure
  private String projectId; // GCP

  private String billingAccountId;
  private List<ServiceScope> serviceScopes;// the service and related metrics that will be fetched

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

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

  public List<ServiceScope> getServiceScopes() {
    return serviceScopes;
  }

  public void setServiceScopes(List<ServiceScope> scopes) {
    this.serviceScopes = scopes;
  }

}
