package com.cloudsherpa.service.webhooks.events.threshold;

import com.cloudsherpa.service.webhooks.events.WebhookPayload;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ThresholdAlertPayload(
    UUID resourceId,
    String metricName,
    BigDecimal observedValue,
    String unit,
    String operator,
    BigDecimal thresholdValue,
    String severity,
    OffsetDateTime periodStart,
    OffsetDateTime periodEnd)
    implements WebhookPayload {}
