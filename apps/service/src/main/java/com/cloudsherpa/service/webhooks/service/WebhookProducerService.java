package com.cloudsherpa.service.webhooks.service;

import com.cloudsherpa.lib.entities.PendingWebhookEvent;
import com.cloudsherpa.lib.repositories.PendingWebhookEventRepository;
import com.cloudsherpa.service.webhooks.events.WebhookEvent;
import com.cloudsherpa.service.webhooks.queue.WebhookEventQueue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

  public void produceEvent(WebhookEvent<?> event) {
    // Write to pending in DB
    PendingWebhookEvent pendingEvent = fromWebhookEvent(event);
    pendingWebhookEventRepository.save(pendingEvent);

    // Offer to queue
    eventQueue.offerEvent(event);
  }

  private PendingWebhookEvent fromWebhookEvent(WebhookEvent<?> event) {
    JsonNode payload = objectMapper.valueToTree(event.data());
    return new PendingWebhookEvent(
        UUID.randomUUID(),
        event.userId(),
        event.cloudAccountId(),
        event.type(),
        event.timestamp(),
        payload);
  }
}
