package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery;

import com.cloudsherpa.ingestion.billing.BillingIngestionServiceInterface;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBillingContext;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBillingIngestionStep;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GcpBillingIngestionService implements BillingIngestionServiceInterface {

  private final List<GcpBillingIngestionStep> gcpBillingIngestionSteps;

  public GcpBillingIngestionService(List<GcpBillingIngestionStep> gcpBillingIngestionSteps) {
    this.gcpBillingIngestionSteps = gcpBillingIngestionSteps;
  }

  public void execute(String userId, String configId) {
    GcpBillingContext context =
        new GcpBillingContext(UUID.fromString(userId), UUID.fromString(configId));

    for (GcpBillingIngestionStep gcpBillingIngestionStep : gcpBillingIngestionSteps) {
      gcpBillingIngestionStep.execute(context);
    }
  }
}
