package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

public class CreditProcessingState {
  private boolean hasCredits = false;
  private boolean processed = false;

  public boolean getProcessed() {
    return processed;
  }

  public void setProcessed(boolean processed) {
    this.processed = processed;
  }

  public boolean getHasCredits() {
    return hasCredits;
  }

  public void setHasCredits(boolean hasCredits) {
    this.hasCredits = hasCredits;
  }
}
