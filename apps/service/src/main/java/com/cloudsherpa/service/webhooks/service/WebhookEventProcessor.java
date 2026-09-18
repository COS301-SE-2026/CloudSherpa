package com.cloudsherpa.service.webhooks.service;

import com.cloudsherpa.lib.entities.PendingWebhookEvent;
import com.cloudsherpa.lib.entities.Webhook;
import com.cloudsherpa.lib.repositories.PendingWebhookEventRepository;
import com.cloudsherpa.lib.repositories.WebhookRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WebhookEventProcessor {

  private final PendingWebhookEventRepository pendingWebhookEventRepository;
  private final WebhookRepository webhookRepository;

  public WebhookEventProcessor(
      PendingWebhookEventRepository pendingWebhookEventRepository,
      WebhookRepository webhookRepository) {
    this.pendingWebhookEventRepository = pendingWebhookEventRepository;
    this.webhookRepository = webhookRepository;
  }

  @Transactional
  public void createDeliveries(UUID eventId) {
    PendingWebhookEvent pendingWebhookEvent =
        pendingWebhookEventRepository.findByIdForUpdate(eventId).orElse(null);

    if (pendingWebhookEvent == null) {
      return;
    }

    List<Webhook> subscribedWebhooks =
        webhookRepository.findByEventType(pendingWebhookEvent.getEventType());

    if (subscribedWebhooks.isEmpty()) {
      return;
    }
  }
}
