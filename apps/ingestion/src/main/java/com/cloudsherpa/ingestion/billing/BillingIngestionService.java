package com.cloudsherpa.ingestion.billing;

public interface BillingIngestionService {
  public void execute(String userId, String configId);
}
