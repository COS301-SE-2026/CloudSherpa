package com.cloudsherpa.ingestion.billing.provider.aws.cur.normalization;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.CostRecordNormalizer;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.exceptions.NormalizationException;
import com.cloudsherpa.lib.entities.ChargeTypeEnum;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.cloudsherpa.lib.entities.ProviderEnum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.example.data.simple.NanoTime;
import org.apache.parquet.io.api.Binary;

public class AwsCurParquetRecordNormalizer
    implements CostRecordNormalizer<GenericRecord, BillingExport> {

  private static final long JULIAN_DAY_OF_UNIX_EPOCH = 2_440_588L;

  @Override
  public NormalizedCosts normalize(GenericRecord costRecord, BillingExport export)
      throws NormalizationException {
    NormalizedCosts normalized = new NormalizedCosts();

    normalized.setExecutionId(getExecutionId(export));
    normalized.setProvider(getProvider(costRecord));
    normalized.setChargeId(getChargeId(costRecord));
    normalized.setResourceId(getResourceId(costRecord));
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
  public String getChargeId(GenericRecord costRecord) {
    return getResourceId(costRecord) + "%%%" + getServiceName(costRecord);
  }

  @Override
  public String getResourceId(GenericRecord costRecord) {
    Object value = costRecord.get("line_item_resource_id");

    if (value == null) {
      return null;
    }

    return value.toString();
  }

  @Override
  public ProviderEnum getProvider(GenericRecord costRecord) {
    return ProviderEnum.AWS;
  }

  @Override
  public String getBillingAccountId(GenericRecord costRecord) {
    Object value = costRecord.get("line_item_usage_account_id");
    if (value == null) {
      throw new NormalizationException("line_item_usage_account_id", "Null value");
    }
    return value.toString();
  }

  @Override
  public ChargeTypeEnum getChargeType(GenericRecord costRecord) {
    Object value = costRecord.get("line_item_line_item_type");

    if (value != null && "Usage".equals(value.toString())) {
      return ChargeTypeEnum.Usage;
    }

    return ChargeTypeEnum.Other;
  }

  @Override
  public String getServiceName(GenericRecord costRecord) {
    Object value = costRecord.get("product_servicecode");

    if (value == null) {
      throw new NormalizationException("product_servicecode", "Null value");
    }

    return value.toString();
  }

  @Override
  public BigDecimal getCostAmount(GenericRecord costRecord) {
    Object value = costRecord.get("line_item_unblended_cost");

    if (value instanceof BigDecimal decimal) {
      return decimal;
    }

    if (value instanceof Number number) {
      return BigDecimal.valueOf(number.doubleValue());
    }

    throw new NormalizationException("line_item_unblended_cost", "Unsupported type or null value");
  }

  @Override
  public OffsetDateTime getUsageStartTime(GenericRecord costRecord) {
    return getTimestamp(costRecord, "line_item_usage_start_date");
  }

  @Override
  public OffsetDateTime getUsageEndTime(GenericRecord costRecord) {
    return getTimestamp(costRecord, "line_item_usage_end_date");
  }

  private OffsetDateTime getTimestamp(GenericRecord costRecord, String fieldName) {
    Object value = costRecord.get(fieldName);

    if (value instanceof GenericData.Fixed fixed) {
      Binary binary = Binary.fromConstantByteArray(fixed.bytes());
      return int96ToOffsetDateTime(binary);
    }

    throw new NormalizationException(fieldName, "Unsupported type or null value");
  }

  private OffsetDateTime int96ToOffsetDateTime(Binary binary) {
    NanoTime nanoTime = NanoTime.fromBinary(binary);

    long epochDay = nanoTime.getJulianDay() - JULIAN_DAY_OF_UNIX_EPOCH;

    LocalDate date = LocalDate.ofEpochDay(epochDay);
    LocalTime time = LocalTime.ofNanoOfDay(nanoTime.getTimeOfDayNanos());

    return OffsetDateTime.of(date, time, ZoneOffset.UTC);
  }
}
