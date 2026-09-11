package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.normalization;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.CostRecordNormalizer;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.exceptions.NormalizationException;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import com.cloudsherpa.lib.entities.ChargeTypeEnum;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AzureBillingNormalizer implements CostRecordNormalizer<RawBillingRow, BillingExport> {

  private final ObjectMapper objectMapper;

  public AzureBillingNormalizer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public NormalizedCosts normalize(RawBillingRow costRecord, BillingExport export)
      throws NormalizationException {
    NormalizedCosts normalized = new NormalizedCosts();

    normalized.setCostId(getCostId(costRecord));
    normalized.setExecutionId(getExecutionId(export));
    normalized.setChargeId(getChargeId(costRecord));
    normalized.setResourceId(getResourceId(costRecord));
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
  public String getCostId(RawBillingRow costRecord) {
    return generateRowHash(costRecord);
  }

  @Override
  public UUID getExecutionId(BillingExport export) {
    throw new UnsupportedOperationException("BillingExport not implemented yet for Azure");
  }

  @Override
  public String getChargeId(RawBillingRow costRecord) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public String getResourceId(RawBillingRow costRecord) {
    return costRecord.resourceId();
  }

  @Override
  public ProviderEnum getProvider(RawBillingRow costRecord) {
    return ProviderEnum.AZURE;
  }

  @Override
  public String getBillingAccountId(RawBillingRow costRecord) {
    return costRecord.billingAccountId();
  }

  @Override
  public ChargeTypeEnum getChargeType(RawBillingRow costRecord) {
    if (costRecord.chargeType().equals("Usage")) {
      return ChargeTypeEnum.Usage;
    } else {
      return ChargeTypeEnum.Other;
    }
  }

  @Override
  public String getServiceName(RawBillingRow costRecord) {
    // Ughie: think service name might have to be comprised of multiple records
    return costRecord.consumedService();
  }

  @Override
  public BigDecimal getCostAmount(RawBillingRow costRecord) {
    return costRecord.costInPricingCurrency();
  }

  @Override
  public OffsetDateTime getUsageStartTime(RawBillingRow costRecord) {
    return OffsetDateTime.of(costRecord.date(), LocalTime.MIN, ZoneOffset.UTC);
  }

  @Override
  public OffsetDateTime getUsageEndTime(RawBillingRow costRecord) {
    return OffsetDateTime.of(costRecord.date(), LocalTime.MAX, ZoneOffset.UTC);
  }

  private String generateRowHash(RawBillingRow rawBillingRow) {
    try {
      byte[] bytes = objectMapper.writeValueAsBytes(rawBillingRow);

      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(bytes));
    } catch (JsonProcessingException | NoSuchAlgorithmException e) {
      throw new NormalizationException("costId", "Failed to hash export row: " + e.getMessage());
    }
  }
}
