package com.cloudsherpa.service.webhooks.service;

import com.cloudsherpa.lib.entities.PendingWebhookEvent;
import com.cloudsherpa.lib.entities.Webhook;
import com.cloudsherpa.lib.entities.WebhookDelivery;
import com.cloudsherpa.lib.entities.WebhookDeliveryStatusEnum;
import com.cloudsherpa.lib.repositories.PendingWebhookEventRepository;
import com.cloudsherpa.lib.repositories.WebhookDeliveryRepository;
import com.cloudsherpa.lib.repositories.WebhookRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WebhookEventProcessor {

  private final PendingWebhookEventRepository pendingWebhookEventRepository;
  private final WebhookRepository webhookRepository;
  private final WebhookDeliveryRepository webhookDeliveryRepository;

  public WebhookEventProcessor(
      PendingWebhookEventRepository pendingWebhookEventRepository,
      WebhookRepository webhookRepository,
      WebhookDeliveryRepository webhookDeliveryRepository) {
    this.pendingWebhookEventRepository = pendingWebhookEventRepository;
    this.webhookRepository = webhookRepository;
    this.webhookDeliveryRepository = webhookDeliveryRepository;
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

    for (Webhook webhook : subscribedWebhooks) {
      webhookDeliveryRepository.save(fromPendingEvent(pendingWebhookEvent, webhook.getWebhookId()));
    }

    pendingWebhookEventRepository.delete(pendingWebhookEvent);
  }

  private WebhookDelivery fromPendingEvent(
      PendingWebhookEvent pendingWebhookEvent, UUID webhookId) {
    return new WebhookDelivery(
        UUID.randomUUID(),
        webhookId,
        pendingWebhookEvent.getEventId(),
        pendingWebhookEvent.getCloudAccountId(),
        pendingWebhookEvent.getEventType(),
        pendingWebhookEvent.getEventTimestamp(),
        pendingWebhookEvent.getPayload(),
        WebhookDeliveryStatusEnum.PENDING,
        null,
        0);
  }
}
