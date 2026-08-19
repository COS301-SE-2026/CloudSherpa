package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.CostRecordNormalizer;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.exceptions.NormalizationException;
import com.cloudsherpa.lib.entities.ChargeTypeEnum;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GcpBigQueryNormalizer
    implements CostRecordNormalizer<GcpBigQueryBillingRecord, BillingExport> {

  private final Logger logger = // NOSONAR will use later
      LoggerFactory.getLogger(GcpBigQueryNormalizer.class);

  // Does not make sense to include billingId in response since it is already
  // required to make the
  // query, hence the billingId is
  // set via a setter and the getBillingAccountId returns this attribute. (saves
  // query costs at
  // expense of extra normalization setter step)
  private String billingId;

  @Override
  public NormalizedCosts normalize(
      GcpBigQueryBillingRecord gcpBillingRecord, BillingExport billingExport)
      throws NormalizationException {
    NormalizedCosts normalized = new NormalizedCosts();

    normalized.setCostId(getCostId(gcpBillingRecord));
    normalized.setExecutionId(getExecutionId(billingExport));
    normalized.setChargeId(getChargeId(gcpBillingRecord));
    normalized.setResourceId(getResourceId(gcpBillingRecord));
    normalized.setProvider(getProvider(gcpBillingRecord));
    normalized.setBillingAccountId(getBillingAccountId(gcpBillingRecord));
    normalized.setChargeType(getChargeType(gcpBillingRecord));
    normalized.setServiceName(getServiceName(gcpBillingRecord));
    normalized.setCostAmount(getCostAmount(gcpBillingRecord));
    normalized.setUsageStartTime(getUsageStartTime(gcpBillingRecord));
    normalized.setUsageEndTime(getUsageEndTime(gcpBillingRecord));

    return normalized;
  }

  @Override
  public String getCostId(GcpBigQueryBillingRecord gcpBillingRecord) {
    FieldValueList valueList = gcpBillingRecord.fieldValueList();

    StringBuilder gcpCostId = new StringBuilder();

    gcpCostId
        .append(ProviderEnum.GCP)
        .append("%%%")
        .append(billingId)
        .append("%%%")
        .append(valueList.get("project_id").getStringValue())
        .append("%%%")
        .append(valueList.get("service_id").getStringValue())
        .append("%%%")
        .append(valueList.get("sku_id").getStringValue())
        .append("%%%")
        .append(getResourceId(gcpBillingRecord))
        .append("%%%")
        .append(valueList.get("cost_type").getStringValue());

    return gcpCostId.toString();
  }

  @Override
  public UUID getExecutionId(BillingExport export) {
    return export.getUuidExportId();
  }

  @Override
  public OffsetDateTime getUsageStartTime(GcpBigQueryBillingRecord gcpBillingRecord) {
    FieldValueList valueList = gcpBillingRecord.fieldValueList();
    return getTimestamp(valueList.get("usage_start_time").getTimestampValue());
  }

  @Override
  public OffsetDateTime getUsageEndTime(GcpBigQueryBillingRecord gcpBillingRecord) {
    FieldValueList valueList = gcpBillingRecord.fieldValueList();
    return getTimestamp(valueList.get("usage_end_time").getTimestampValue());
  }

  @Override
  public String getChargeId(GcpBigQueryBillingRecord gcpBillingRecord) {
    FieldValueList valueList = gcpBillingRecord.fieldValueList();
    StringBuilder chargeId = new StringBuilder();

    String resourceName;

    if (valueList.get("resource_name").isNull()) {
      resourceName = "null";
    } else {
      resourceName = valueList.get("resource_name").getStringValue();
    }

    chargeId
        .append(resourceName)
        .append("%%%")
        .append(valueList.get("service_description").getStringValue().replace(" ", "_"));

    return chargeId.toString();
  }

  @Override
  public String getResourceId(GcpBigQueryBillingRecord gcpBillingRecord) {
    FieldValueList valueList = gcpBillingRecord.fieldValueList();

    if (valueList.get("resource_global_name").isNull()) {
      return "null";
    }

    return valueList.get("resource_global_name").getStringValue();
  }

  @Override
  public ProviderEnum getProvider(GcpBigQueryBillingRecord gcpBillingRecord) {
    return ProviderEnum.GCP;
  }

  @Override
  public String getBillingAccountId(GcpBigQueryBillingRecord gcpBillingRecord) {

    if (billingId == null) {
      throw new IllegalArgumentException(
          "The billing account ID for GCP billing exports needs to be explicitly set for a "
              + "gcpBillingRecord to be normalized");
    }

    return billingId;
  }

  @Override
  public ChargeTypeEnum getChargeType(GcpBigQueryBillingRecord gcpBillingRecord) {

    CreditProcessingState creditProcessingState = gcpBillingRecord.creditProcessingState();

    if (shouldEmitCreditRecord(creditProcessingState)) {
      return ChargeTypeEnum.Other;
    } else {
      return ChargeTypeEnum.Usage;
    }
  }

  @Override
  public String getServiceName(GcpBigQueryBillingRecord gcpBillingRecord) {
    FieldValueList valueList = gcpBillingRecord.fieldValueList();
    return valueList.get("service_description").getStringValue();
  }

  @Override
  public BigDecimal getCostAmount(GcpBigQueryBillingRecord gcpBillingRecord) {
    FieldValueList valueList = gcpBillingRecord.fieldValueList();
    CreditProcessingState creditProcessingState = gcpBillingRecord.creditProcessingState();

    if (!shouldEmitCreditRecord(creditProcessingState)) {
      return valueList.get("cost").getNumericValue();
    }

    BigDecimal costAmount = BigDecimal.ZERO;

    for (FieldValue creditValue : valueList.get("credits").getRepeatedValue()) {
      // Refer to query (GcpBillingQueryStep), credit amount will always be the first element of the
      // struct hence we can index by 0
      BigDecimal creditAmount = creditValue.getRecordValue().get(0).getNumericValue();

      if (creditAmount != null) {
        costAmount = costAmount.add(creditAmount);
      }
    }

    return costAmount;
  }

  // Class specific public method to set billing id. If this is not called before
  // the normalize
  // gcpBillingRecord is called
  // an exception will be thrown during normaization.
  public void setBillingId(String billingId) {
    this.billingId = billingId;
  }

  private OffsetDateTime getTimestamp(long nanos) {
    // Division by 1000 since BigQuery stores timestamps in nanoseconds
    Instant timestampInstant = Instant.ofEpochMilli(nanos / 1000);
    ZoneId zoneId = ZoneId.of("UTC");
    return OffsetDateTime.ofInstant(timestampInstant, zoneId);
  }

  private boolean shouldEmitCreditRecord(CreditProcessingState state) {
    return state.getHasCredits() && !state.getProcessed();
  }
}
