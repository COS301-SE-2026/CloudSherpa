package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBillingContext;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.google.cloud.bigquery.FieldValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GcpBigQueryNormalizationService {

  private final Logger logger = LoggerFactory.getLogger(GcpBigQueryNormalizationService.class);
  private final SherpaDbPersistenceService persistenceService;

  public GcpBigQueryNormalizationService(SherpaDbPersistenceService persistenceService) {
    this.persistenceService = persistenceService;
  }

  public void normalize(GcpBillingContext context) {
    GcpBigQueryNormalizer gcpBigQueryNormalizer = new GcpBigQueryNormalizer();
    gcpBigQueryNormalizer.setBillingId(context.getBillingConfig().billingAccountId());

    for (FieldValueList fieldValueList : context.getTableResult().getValues()) {
      NormalizedCosts normalizedCosts =
          gcpBigQueryNormalizer.normalize(fieldValueList, context.getBillingExport());
      logger.info(
          "Normalized GCP BigQuery cost record: costId={}, executionId={}, resourceId={}, chargeId={}, provider={}, billingAccountId={}, serviceName={}, chargeType={}, costAmount={}, currency={}, usageStartTime={}, usageEndTime={}, metadata={}",
          normalizedCosts.getCostId(),
          normalizedCosts.getExecutionId(),
          normalizedCosts.getResourceId(),
          normalizedCosts.getChargeId(),
          normalizedCosts.getProvider(),
          normalizedCosts.getBillingAccountId(),
          normalizedCosts.getServiceName(),
          normalizedCosts.getChargeType(),
          normalizedCosts.getCostAmount(),
          normalizedCosts.getCurrency(),
          normalizedCosts.getUsageStartTime(),
          normalizedCosts.getUsageEndTime(),
          normalizedCosts.getMetadata());
      persistenceService.recordCost(normalizedCosts, context.getUserId());
    }
  }
}
