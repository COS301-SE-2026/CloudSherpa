package com.cloudsherpa.ingestion.connector;

import java.time.Instant;

import java.util.List;

public class IngestionRequest {

  private Instant from;
  private Instant to;
  private CloudCredentials credentials;
  private List<AccountScope> scopes;
  private boolean includeBilling;
  private boolean includeUsage;

  public Instant getInstantFrom() {
    return from;
  }

  public void setInstantFrom(Instant from) {
    this.from = from;
  }

  public Instant getInstantTo() {
    return to;
  }

  public void setInstantTo(Instant to) {
    this.to = to;
  }

  public CloudCredentials getCredentials() {
    return credentials;
  }

  public void setCredentials(CloudCredentials credentials) {
    this.credentials = credentials;
  }

  public void setScopes(List<AccountScope> scopes) {
    this.scopes = scopes;
  }

  public List<AccountScope> getScopes() {
    return this.scopes;
  }

  public boolean isIncludeBilling() {
    return includeBilling;
  }

  public void setIncludeBilling(boolean includeBilling) {
    this.includeBilling = includeBilling;
  }

  public boolean isIncludeUsage() {
    return includeUsage;
  }

  public void setIncludeUsage(boolean includeUsage) {
    this.includeUsage = includeUsage;
  }
}
