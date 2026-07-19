package com.cloudsherpa.ingestion.billing.provider.aws.cur.normalization;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.CostRecordNormalizer;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.exceptions.NormalizationException;
import com.cloudsherpa.lib.entities.ChargeTypeEnum;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.cloudsherpa.lib.entities.ProviderEnum;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.apache.commons.csv.CSVRecord;

public class AwsCurCsvRecordNormalizer implements CostRecordNormalizer<CSVRecord, BillingExport> {

  @Override
  public NormalizedCosts normalize(CSVRecord costRecord, BillingExport export)
      throws NormalizationException {
    NormalizedCosts normalized = new NormalizedCosts();

    normalized.setExecutionId(getExecutionId(export));
    normalized.setProvider(getProvider(costRecord));
    normalized.setBillingAccountId(getBillingAccountId(costRecord));
    normalized.setChargeType(getChargeType(costRecord));
    normalized.setServiceName(getServiceName(costRecord));
    normalized.setCostAmount(getCostAmount(costRecord));
    normalized.setUsageStartTime(getUsageStartTime(costRecord));
    normalized.setUsageEndTime(getUsageEndTime(costRecord));

    return normalized;
  }

  @Override
  public UUID getExecutionId(BillingExport export) {
    return export.getUuidExportId();
  }

  @Override
  public ProviderEnum getProvider(CSVRecord costRecord) {
    return ProviderEnum.AWS;
  }

  @Override
  public String getBillingAccountId(CSVRecord costRecord) {
    return getRequiredValue(costRecord, "line_item_usage_account_id");
  }

  @Override
  public ChargeTypeEnum getChargeType(CSVRecord costRecord) {
    String value = getOptionalValue(costRecord, "line_item_line_item_type");

    if ("Usage".equals(value)) {
      return ChargeTypeEnum.Usage;
    }

    return ChargeTypeEnum.Other;
  }

  @Override
  public String getServiceName(CSVRecord costRecord) {
    return getRequiredValue(costRecord, "product_servicecode");
  }

  @Override
  public BigDecimal getCostAmount(CSVRecord costRecord) {
    String value = getRequiredValue(costRecord, "line_item_unblended_cost");

    try {
      return new BigDecimal(value);
    } catch (NumberFormatException exception) {
      throw new NormalizationException("line_item_unblended_cost", "Unsupported value");
    }
  }

  @Override
  public OffsetDateTime getUsageStartTime(CSVRecord costRecord) {
    return getTimestamp(costRecord, "line_item_usage_start_date");
  }

  @Override
  public OffsetDateTime getUsageEndTime(CSVRecord costRecord) {
    return getTimestamp(costRecord, "line_item_usage_end_date");
  }

  private OffsetDateTime getTimestamp(CSVRecord costRecord, String fieldName) {
    String value = getRequiredValue(costRecord, fieldName);

    try {
      return OffsetDateTime.parse(value);
    } catch (RuntimeException exception) {
      throw new NormalizationException(fieldName, "Unsupported value");
    }
  }

  private String getRequiredValue(CSVRecord costRecord, String fieldName) {
    String value = getOptionalValue(costRecord, fieldName);

    if (value == null || value.isBlank()) {
      throw new NormalizationException(fieldName, "Null value");
    }

    return value;
  }

  private String getOptionalValue(CSVRecord costRecord, String fieldName) {
    if (costRecord.isMapped(fieldName) && costRecord.isSet(fieldName)) {
      return costRecord.get(fieldName);
    }

    return null;
  }
}
