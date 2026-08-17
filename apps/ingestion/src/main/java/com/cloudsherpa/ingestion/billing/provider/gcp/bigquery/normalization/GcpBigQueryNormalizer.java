package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.normalization;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.CostRecordNormalizer;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.exceptions.NormalizationException;
import com.cloudsherpa.lib.entities.ChargeTypeEnum;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.google.cloud.bigquery.FieldValueList;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class GcpBigQueryNormalizer implements CostRecordNormalizer<FieldValueList, BillingExport> {

  // Does not make sense to include billingId in response since it is already required to make the
  // query, hence the billingId is
  // set via a setter and the getBillingAccountId returns this attribute. (saves query costs at
  // expense of extra normalization setter step)
  private String billingId;

  @Override
  public NormalizedCosts normalize(FieldValueList valueList, BillingExport billingExport)
      throws NormalizationException {
    NormalizedCosts normalized = new NormalizedCosts();

    normalized.setCostId(getCostId(valueList));
    normalized.setExecutionId(getExecutionId(billingExport));
    normalized.setChargeId(getChargeId(valueList));
    normalized.setResourceId(getResourceId(valueList));
    normalized.setProvider(getProvider(valueList));
    normalized.setBillingAccountId(getBillingAccountId(valueList));
    normalized.setChargeType(getChargeType(valueList));
    normalized.setServiceName(getServiceName(valueList));
    normalized.setCostAmount(getCostAmount(valueList));
    normalized.setUsageStartTime(getUsageStartTime(valueList));
    normalized.setUsageEndTime(getUsageEndTime(valueList));

    return normalized;
  }

  @Override
  public String getCostId(FieldValueList valueList) {

    StringBuilder gcpCostId = new StringBuilder();

    gcpCostId
        .append(ProviderEnum.GCP)
        .append("%%%")
        .append(valueList.get("billing_account_id").getStringValue())
        .append("%%%")
        .append(valueList.get("project_id").getStringValue())
        .append("%%%")
        .append(valueList.get("service_id").getStringValue())
        .append("%%%")
        .append(valueList.get("sku_id").getStringValue())
        .append("%%%")
        .append(valueList.get("resource_global_name").getStringValue())
        .append("%%%")
        .append(valueList.get("cost_type").getStringValue());

    return gcpCostId.toString();
  }

  @Override
  public UUID getExecutionId(BillingExport export) {
    return export.getUuidExportId();
  }

  @Override
  public OffsetDateTime getUsageStartTime(FieldValueList valueList) {
    return getTimestamp(valueList.get("usage_start_time").getTimestampValue());
  }

  @Override
  public OffsetDateTime getUsageEndTime(FieldValueList valueList) {
    return getTimestamp(valueList.get("usage_end_time").getTimestampValue());
  }

  @Override
  public String getChargeId(FieldValueList valueList) {
    StringBuilder chargeId = new StringBuilder();

    chargeId
        .append(valueList.get("resource_name"))
        .append("%%%")
        .append(valueList.get("service_description").getStringValue().replace(" ", "_"));

    return chargeId.toString();
  }

  @Override
  public String getResourceId(FieldValueList valueList) {
    return valueList.get("resource_global_name").getStringValue();
  }

  @Override
  public ProviderEnum getProvider(FieldValueList valueList) {
    return ProviderEnum.GCP;
  }

  @Override
  public String getBillingAccountId(FieldValueList valueList) {

    if (billingId == null) {
      throw new IllegalArgumentException(
          "The billing account ID for GCP billing exports needs to be explicitly set for a record to be normalized");
    }

    return billingId;
  }

  @Override
  public ChargeTypeEnum getChargeType(FieldValueList valueList) {
    // Can only be Usage or Other. Current conceptual mapping is
    // !credit = Usage else Other
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public String getServiceName(FieldValueList valueList) {
    return valueList.get("service_description").getStringValue();
  }

  @Override
  public BigDecimal getCostAmount(FieldValueList valueList) {
    return valueList.get("cost").getNumericValue();
  }

  // Class specific public method to set billing id. If this is not called before the normalize
  // record is called
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
}
