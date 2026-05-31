package com.cloudsherpa.ingestion.models;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class IngestionRequestEvent {

  private List<AccountScope> scopes;

  private CloudCredentials credentials;

  private Instant from;
  private Instant to;
  private Integer period;

  private boolean includeBilling;
  private boolean includeUsage;

  private UUID userId;

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public List<AccountScope> getScopes() {
    return scopes;
  }

  public void setScopes(List<AccountScope> scopes) {
    this.scopes = scopes;
  }

  public CloudCredentials getCredentials() {
    return credentials;
  }

  public void setCredentials(CloudCredentials credentials) {
    this.credentials = credentials;
  }

  public Instant getFrom() {
    return from;
  }

  public void setFrom(Instant from) {
    this.from = from;
  }

  public Instant getTo() {
    return to;
  }

  public void setPeriod(Integer period) {
    this.period = period;
  }

  public Integer getPeriod() {
    return period;
  }

  public void setTo(Instant to) {
    this.to = to;
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
