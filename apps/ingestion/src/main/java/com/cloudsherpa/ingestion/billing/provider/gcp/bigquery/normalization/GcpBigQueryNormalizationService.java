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

      GcpBigQueryBillingRecord gcpBigQueryBillingRecord =
          new GcpBigQueryBillingRecord(fieldValueList, new CreditProcessingState());

      if (!fieldValueList.get("credits").getRecordValue().isEmpty()) {
        gcpBigQueryBillingRecord.creditProcessingState().setHasCredits(true);
      }

      if (gcpBigQueryBillingRecord.creditProcessingState().getHasCredits()) {
        NormalizedCosts normalizedCosts =
            gcpBigQueryNormalizer.normalize(gcpBigQueryBillingRecord, context.getBillingExport());
        gcpBigQueryBillingRecord.creditProcessingState().setProcessed(true);
        persistenceService.recordCost(normalizedCosts, null);
      }

      NormalizedCosts normalizedCosts =
          gcpBigQueryNormalizer.normalize(gcpBigQueryBillingRecord, context.getBillingExport());
      persistenceService.recordCost(normalizedCosts, context.getUserId());
    }
  }
}
