package com.cloudsherpa.service.webhooks.consumers;

import com.cloudsherpa.lib.entities.PendingWebhookEvent;
import com.cloudsherpa.lib.entities.Webhook;
import com.cloudsherpa.lib.entities.WebhookDelivery;
import com.cloudsherpa.lib.entities.WebhookDeliveryStatusEnum;
import com.cloudsherpa.lib.repositories.PendingWebhookEventRepository;
import com.cloudsherpa.lib.repositories.WebhookDeliveryRepository;
import com.cloudsherpa.lib.repositories.WebhookRepository;
import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
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
  public List<DeliveryTask> createDeliveries(UUID eventId) {
    PendingWebhookEvent pendingWebhookEvent =
        pendingWebhookEventRepository.findByIdForUpdate(eventId).orElse(null);

    if (pendingWebhookEvent == null) {
      return List.of();
    }

    List<Webhook> subscribedWebhooks =
        webhookRepository.findSubscribed(
            pendingWebhookEvent.getEventType(), pendingWebhookEvent.getCloudAccountId());

    List<DeliveryTask> deliveryTasks = new ArrayList<>();
    for (Webhook webhook : subscribedWebhooks) {
      WebhookDelivery delivery = fromPendingEvent(pendingWebhookEvent, webhook);
      webhookDeliveryRepository.save(delivery);
      deliveryTasks.add(
          new DeliveryTask(pendingWebhookEvent.getTenantId(), delivery.getWebhookDeliveryId()));
    }

    pendingWebhookEventRepository.delete(pendingWebhookEvent);
    return deliveryTasks;
  }

  private WebhookDelivery fromPendingEvent(
      PendingWebhookEvent pendingWebhookEvent, Webhook webhook) {
    return new WebhookDelivery(
        UUID.randomUUID(),
        webhook,
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
