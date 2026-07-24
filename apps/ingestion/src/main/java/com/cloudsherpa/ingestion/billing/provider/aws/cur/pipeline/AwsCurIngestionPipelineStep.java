package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

public interface AwsCurIngestionPipelineStep {
  public void execute(AwsCurContext context);
}
