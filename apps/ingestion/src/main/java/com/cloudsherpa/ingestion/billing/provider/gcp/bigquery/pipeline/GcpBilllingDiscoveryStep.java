package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class GcpBilllingDiscoveryStep implements GcpBillingIngestionStep {
  public void execute(GcpBillingContext context) {
    // tbd
  }
}
