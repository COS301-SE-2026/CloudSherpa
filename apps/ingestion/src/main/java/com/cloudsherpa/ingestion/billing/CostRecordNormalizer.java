package com.cloudsherpa.ingestion.billing;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.exceptions.NormalizationException;
import com.cloudsherpa.lib.entities.ChargeTypeEnum;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.cloudsherpa.lib.entities.ProviderEnum;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface CostRecordNormalizer<T, E> {

  NormalizedCosts normalize(T costRecord, E export) throws NormalizationException;

  UUID getExecutionId(E export);

  public String getChargeId(T costRecord);

  String getResourceId(T costRecord);

  ProviderEnum getProvider(T costRecord);

  String getBillingAccountId(T costRecord);

  ChargeTypeEnum getChargeType(T costRecord);

  String getServiceName(T costRecord);

  BigDecimal getCostAmount(T costRecord);

  OffsetDateTime getUsageStartTime(T costRecord);

  OffsetDateTime getUsageEndTime(T costRecord);
}
