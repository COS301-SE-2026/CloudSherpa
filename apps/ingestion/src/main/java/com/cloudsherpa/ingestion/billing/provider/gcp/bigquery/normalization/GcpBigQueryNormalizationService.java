package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.exceptions.NormalizationException;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBillingContext;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.google.cloud.bigquery.FieldValueList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class GcpBigQueryNormalizationService {

  private final Logger logger = LoggerFactory.getLogger(GcpBigQueryNormalizationService.class);
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

    Iterator<FieldValueList> it = context.getTableResult().getValues().iterator();

    while (it.hasNext()) {
      List<NormalizedCosts> batch = normalizeBatch(it, 100, gcpBigQueryNormalizer, context);

      if (!batch.isEmpty()) {
        persistenceService.recordCosts(batch, context.getUserId());
      }
    }
  }

  private List<NormalizedCosts> normalizeBatch(
      Iterator<FieldValueList> it,
      int batchSize,
      GcpBigQueryNormalizer gcpBigQueryNormalizer,
      GcpBillingContext context) {
    List<NormalizedCosts> currentBatch = new ArrayList<>();

    while (it.hasNext() && currentBatch.size() < batchSize) {
      List<NormalizedCosts> normalizedCost =
          normalizeRow(it.next(), gcpBigQueryNormalizer, context);
      if (!normalizedCost.isEmpty()) {
        currentBatch.addAll(normalizedCost);
      }
    }

    return currentBatch;
  }

  private List<NormalizedCosts> normalizeRow(
      FieldValueList fieldValueList,
      GcpBigQueryNormalizer gcpBigQueryNormalizer,
      GcpBillingContext context) {
    List<NormalizedCosts> normalizationResult = new ArrayList<>();

    GcpBigQueryBillingRecord gcpBigQueryBillingRecord =
        new GcpBigQueryBillingRecord(fieldValueList, new CreditProcessingState());

    if (!fieldValueList.get("credits").getRepeatedValue().isEmpty()) {
      gcpBigQueryBillingRecord.creditProcessingState().setHasCredits(true);
    }

    try {
      if (gcpBigQueryBillingRecord.creditProcessingState().getHasCredits()) {
        NormalizedCosts normalizedCosts =
            gcpBigQueryNormalizer.normalize(gcpBigQueryBillingRecord, context.getBillingExport());
        gcpBigQueryBillingRecord.creditProcessingState().setProcessed(true);
        normalizationResult.add(normalizedCosts);
      }

      normalizationResult.add(
          gcpBigQueryNormalizer.normalize(gcpBigQueryBillingRecord, context.getBillingExport()));
      return normalizationResult;
    } catch (NormalizationException e) {
      logger.error(e.getMessage(), e);
      return normalizationResult;
    }
  }
}
