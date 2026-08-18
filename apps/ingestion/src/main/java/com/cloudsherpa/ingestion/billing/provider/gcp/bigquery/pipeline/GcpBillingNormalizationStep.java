package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization.GcpBigQueryNormalizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class GcpBillingNormalizationStep implements GcpBillingIngestionStep {

  private final Logger logger = // NOSONAR will use logger in future
      LoggerFactory.getLogger(GcpBillingNormalizationStep.class);
  private final GcpBigQueryNormalizationService gcpBigQueryNormalizationService;

  public GcpBillingNormalizationStep(
      GcpBigQueryNormalizationService gcpBigQueryNormalizationService) {
    this.gcpBigQueryNormalizationService = gcpBigQueryNormalizationService;
  }

  public void execute(GcpBillingContext context) {
    gcpBigQueryNormalizationService.normalize(context);
  }
}
