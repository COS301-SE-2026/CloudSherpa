package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery;

import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBillingContext;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBillingIngestionStep;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GcpBillingIngestionService {

  private final List<GcpBillingIngestionStep> gcpBillingIngestionSteps;

  public GcpBillingIngestionService(List<GcpBillingIngestionStep> gcpBillingIngestionSteps) {
    this.gcpBillingIngestionSteps = gcpBillingIngestionSteps;
  }

  public void execute() {
    GcpBillingContext context = new GcpBillingContext();

    for (GcpBillingIngestionStep gcpBillingIngestionStep : gcpBillingIngestionSteps) {
      gcpBillingIngestionStep.execute(context);
    }
  }
}
