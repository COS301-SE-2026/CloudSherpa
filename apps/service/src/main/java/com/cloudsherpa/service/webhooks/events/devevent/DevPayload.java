package com.cloudsherpa.service.webhooks.events.devevent;

import com.cloudsherpa.service.webhooks.events.WebhookPayload;
import java.math.BigDecimal;
import java.time.Instant;

// Payload for dev.event for use during dev and illustration of payload structure
public record DevPayload(String devString, BigDecimal devDecimal, Instant devTime)
    implements WebhookPayload {}
