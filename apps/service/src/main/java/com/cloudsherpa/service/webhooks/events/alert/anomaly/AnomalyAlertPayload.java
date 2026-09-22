package com.cloudsherpa.service.webhooks.events.alert.anomaly;

import com.cloudsherpa.service.webhooks.events.WebhookPayload;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AnomalyAlertPayload(
    String resourceId,
    String resourceName,
    String metricName,
    BigDecimal averageValue,
    BigDecimal standardDeviation,
    double zScore,
    String severity,
    OffsetDateTime periodStart,
    OffsetDateTime periodEnd)
    implements WebhookPayload {}
