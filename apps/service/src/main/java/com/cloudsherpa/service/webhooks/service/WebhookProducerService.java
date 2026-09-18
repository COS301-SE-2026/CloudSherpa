package com.cloudsherpa.service.webhooks.service;

import com.cloudsherpa.lib.entities.PendingWebhookEvent;
import com.cloudsherpa.lib.repositories.PendingWebhookEventRepository;
import com.cloudsherpa.service.webhooks.events.WebhookPayload;
import com.cloudsherpa.service.webhooks.queue.WebhookEventQueue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WebhookProducerService {
  private final PendingWebhookEventRepository pendingWebhookEventRepository;
  private final WebhookEventQueue eventQueue;
  private final ObjectMapper objectMapper;

  WebhookProducerService(
      PendingWebhookEventRepository pendingWebhookEventRepository,
      ObjectMapper objectMapper,
      WebhookEventQueue eventQueue) {
    this.pendingWebhookEventRepository = pendingWebhookEventRepository;
    this.objectMapper = objectMapper;
    this.eventQueue = eventQueue;
  }

  public void produceEvent(
      UUID tenantId, UUID cloudAccountId, String eventType, WebhookPayload payload) {
    // Write to pending in DB
    PendingWebhookEvent pendingEvent =
        constructPendingWebhookEvent(tenantId, cloudAccountId, eventType, payload, Instant.now());
    pendingWebhookEventRepository.save(pendingEvent);

    // Offer to queue
    eventQueue.offerEvent(pendingEvent.getEventId());
  }

  public void retryProductions() {
    List<PendingWebhookEvent> pendingWebhookEvents = pendingWebhookEventRepository.findAll();

    for (PendingWebhookEvent event : pendingWebhookEvents) {
      eventQueue.offerEvent(event.getEventId());
    }
  }

  private PendingWebhookEvent constructPendingWebhookEvent(
      UUID tenantId,
      UUID cloudAccountId,
      String eventType,
      WebhookPayload payload,
      Instant timestamp) {
    JsonNode jsonPayload = objectMapper.valueToTree(payload);
    return new PendingWebhookEvent(
        UUID.randomUUID(), tenantId, cloudAccountId, eventType, timestamp, jsonPayload);
  }
}
