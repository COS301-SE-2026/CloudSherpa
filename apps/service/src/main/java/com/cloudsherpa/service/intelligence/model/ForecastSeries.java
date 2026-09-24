package com.cloudsherpa.service.intelligence.model;

import com.cloudsherpa.service.intelligence.service.billing.BillingForecastValue;
import java.util.Map;

public record ForecastSeries(Map<String, BillingForecastValue> billingForecastSeries) {}
