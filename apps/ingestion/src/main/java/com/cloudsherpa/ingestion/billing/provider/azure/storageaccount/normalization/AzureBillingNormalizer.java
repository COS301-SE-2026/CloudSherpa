package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.normalization;

import com.cloudsherpa.ingestion.billing.CostRecordNormalizer;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.exceptions.NormalizationException;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import com.cloudsherpa.lib.entities.BillingExportExecution;
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
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;

@Component
public class AzureBillingNormalizer
    implements CostRecordNormalizer<RawBillingRow, BillingExportExecution> {

  private final ObjectMapper objectMapper;

  public AzureBillingNormalizer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public NormalizedCosts normalize(RawBillingRow costRecord, BillingExportExecution export)
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
  public UUID getExecutionId(BillingExportExecution export) {
    return export.getId();
  }

  @Override
  public String getChargeId(RawBillingRow costRecord) {
    return getResourceId(costRecord) + "%%%" + getServiceName(costRecord);
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
    String[] splitConsumedService = costRecord.consumedService().split("\\.");
    String[] splitMeterCategory = costRecord.meterCategory().split(" ");
    String[] splitMeterSubCategory = costRecord.meterSubCategory().split(" ");

    Set<String> serviceNameSet =
        Stream.of(splitConsumedService, splitMeterCategory, splitMeterSubCategory)
            .flatMap(Arrays::stream)
            .map(WordUtils::capitalizeFully)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    return String.join(" ", serviceNameSet);
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
