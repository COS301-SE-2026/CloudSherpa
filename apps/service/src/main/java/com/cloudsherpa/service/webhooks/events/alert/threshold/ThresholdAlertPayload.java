package com.cloudsherpa.service.webhooks.events.alert.threshold;

import com.cloudsherpa.service.webhooks.events.WebhookPayload;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ThresholdAlertPayload(
    String resourceId,
    String resourceName,
    String metricName,
    BigDecimal observedValue,
    String unit,
    String operator,
    double thresholdValue,
    String severity,
    OffsetDateTime periodStart,
    OffsetDateTime periodEnd)
    implements WebhookPayload {}
