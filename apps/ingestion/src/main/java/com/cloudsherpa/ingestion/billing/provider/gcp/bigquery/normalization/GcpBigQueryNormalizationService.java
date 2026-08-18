package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline.GcpBillingContext;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.google.cloud.bigquery.FieldValueList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GcpBigQueryNormalizationService {

  private final Logger logger = LoggerFactory.getLogger(GcpBigQueryNormalizationService.class);

  public void normalize(GcpBillingContext context) {
    GcpBigQueryNormalizer gcpBigQueryNormalizer = new GcpBigQueryNormalizer();
    gcpBigQueryNormalizer.setBillingId(context.getBillingConfig().billingAccountId());

    // Why is temp export necessary? A normalizedcost records containes a reference
    // to the billing
    // exports table,
    // hence the related export object is normally passed from the context to be
    // able to obtain the
    // necessary data
    // needed to construct the normalizedcost record. Since the BillingExport is
    // currently a no-go
    // for GCP, this
    // temp export is created to be able to test normalization logic even though the
    // database does
    // not yet support it
    BillingExport tempExport =
        new BillingExport(UUID.randomUUID().toString(), UUID.randomUUID().toString(), List.of());

    for (FieldValueList fieldValueList : context.getTableResult().getValues()) {

      GcpBigQueryBillingRecord gcpBigQueryBillingRecord =
          new GcpBigQueryBillingRecord(fieldValueList, new CreditProcessingState());

      if (!fieldValueList.get("credits").getRecordValue().isEmpty()) {
        gcpBigQueryBillingRecord.creditProcessingState().setHasCredits(true);
      }

      if (gcpBigQueryBillingRecord.creditProcessingState().getHasCredits()) {
        NormalizedCosts normalizedCosts =
            gcpBigQueryNormalizer.normalize(gcpBigQueryBillingRecord, tempExport);
        logger.info(
            "Normalized GCP BigQuery cost record: costId={}, executionId={}, resourceId={}, "
                + "chargeId={}, provider={}, billingAccountId={}, serviceName={}, chargeType={}, "
                + "costAmount={}, currency={}, usageStartTime={}, usageEndTime={}, metadata={}",
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

        gcpBigQueryBillingRecord.creditProcessingState().setProcessed(true);
      }

      NormalizedCosts normalizedCosts =
          gcpBigQueryNormalizer.normalize(gcpBigQueryBillingRecord, tempExport);
      logger.info(
          "Normalized GCP BigQuery cost record: costId={}, executionId={}, resourceId={}, "
              + "chargeId={}, provider={}, billingAccountId={}, serviceName={}, chargeType={}, "
              + "costAmount={}, currency={}, usageStartTime={}, usageEndTime={}, metadata={}",
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
    }
  }
}
