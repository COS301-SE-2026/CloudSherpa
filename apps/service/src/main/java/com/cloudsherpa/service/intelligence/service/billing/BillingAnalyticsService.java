package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class BillingAnalyticsService {

  private final NormalizedCostsRepository normalizedCostsRepository;

  public BillingAnalyticsService(NormalizedCostsRepository normalizedCostsRepository) {
    this.normalizedCostsRepository = normalizedCostsRepository;
  }

  public BillingAnalyticsResult process(
      BillingForecastResult billingForecastResult, Integer historicalDays) {

    return null;
  }

  private String getChargeLabel(String chargeId) {
    // I expect the charge label to maybe be something that requires change, hence it is a method
    // For now this method takes a charge such as i-067d6c946c07882dd%%%AWSDataTransfer and returns
    // AWSDataTransfer (i-067d6c946c07882dd)
    String[] splitChargeId = chargeId.split("%%%");

    StringBuilder chargeLabel = new StringBuilder();

    chargeLabel.append(splitChargeId[1]).append(" (");

    if (splitChargeId[0].equals("null")) {
      chargeLabel.append("No resource");
    } else {
      chargeLabel.append(splitChargeId[0]);
    }

    chargeLabel.append(")");

    return chargeLabel.toString();
  }

  private BigDecimal variance() {
    return null;
  }

  private String primaryCostDriver(Map<String, BigDecimal> individualChargeForecastResults) {
    return individualChargeForecastResults.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  private String highestCostAcceleration() {
    return null;
  }
}
