package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RawBillingRow(
    String billingAccountId,
    LocalDate date,
    String consumedService,
    String meterCategory,
    String meterSubCategory,
    String resourceId,
    String chargeType,
    String billingCurrency,
    BigDecimal costInPricingCurrency) {}
