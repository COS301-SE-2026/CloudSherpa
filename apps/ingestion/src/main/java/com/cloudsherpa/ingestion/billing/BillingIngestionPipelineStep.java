package com.cloudsherpa.ingestion.billing;

public interface BillingIngestionPipelineStep<T> {
  public void execute(T context);
}
