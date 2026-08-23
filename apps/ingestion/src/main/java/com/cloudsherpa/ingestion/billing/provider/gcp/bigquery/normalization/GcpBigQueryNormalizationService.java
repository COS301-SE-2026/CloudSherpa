package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBillingContext;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.google.cloud.bigquery.FieldValueList;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class GcpBigQueryNormalizationService {

  private final SherpaDbPersistenceService persistenceService;
  private final ObjectProvider<GcpBigQueryNormalizer> contexts;

  public GcpBigQueryNormalizationService(
      SherpaDbPersistenceService persistenceService,
      ObjectProvider<GcpBigQueryNormalizer> contexts) {
    this.persistenceService = persistenceService;
    this.contexts = contexts;
  }

  public void normalize(GcpBillingContext context) {
    GcpBigQueryNormalizer gcpBigQueryNormalizer = contexts.getObject();
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
        persistenceService.recordCost(normalizedCosts, context.getUserId());
      }

      NormalizedCosts normalizedCosts =
          gcpBigQueryNormalizer.normalize(gcpBigQueryBillingRecord, context.getBillingExport());
      persistenceService.recordCost(normalizedCosts, context.getUserId());
    }
  }
}
