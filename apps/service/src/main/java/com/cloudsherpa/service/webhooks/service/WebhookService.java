package com.cloudsherpa.service.webhooks.service;

import com.cloudsherpa.lib.entities.Webhook;
import com.cloudsherpa.lib.entities.WebhookDelivery;
import com.cloudsherpa.lib.repositories.WebhookDeliveryRepository;
import com.cloudsherpa.lib.repositories.WebhookRepository;
import com.cloudsherpa.service.webhooks.dto.WebhookDeliveryResponse;
import com.cloudsherpa.service.webhooks.dto.WebhookResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {
  private final WebhookRepository webhookRepository;
  private final WebhookDeliveryRepository webhookDeliveryRepository;

  public WebhookService(
      WebhookRepository webhookRepository, WebhookDeliveryRepository webhookDeliveryRepository) {
    this.webhookRepository = webhookRepository;
    this.webhookDeliveryRepository = webhookDeliveryRepository;
  }

  public List<WebhookResponse> getWebhooks() {
    List<Webhook> webhooks = webhookRepository.findAll();
    return webhooks.stream().map(this::fromWebhook).toList();
  }

  public List<WebhookDeliveryResponse> getWebhookDeliveries() {
    List<WebhookDelivery> webhookDeliveries = webhookDeliveryRepository.findAll();
    return webhookDeliveries.stream().map(this::fromWebhookDelivery).toList();
  }

  private WebhookResponse fromWebhook(Webhook webhook) {
    return new WebhookResponse(
        webhook.getWebhookId(),
        webhook.getWebhookName(),
        webhook.getEndpointUrl(),
        webhook.getEventTypes(),
        webhook.getCloudAccounts(),
        webhook.getWebhookStatus());
  }

  private WebhookDeliveryResponse fromWebhookDelivery(WebhookDelivery webhookDelivery) {
    return new WebhookDeliveryResponse(
        webhookDelivery.getWebhookDeliveryId(),
        webhookDelivery.getEventTimestamp(),
        webhookDelivery.getWebhookId(),
        webhookDelivery.getEventType(),
        webhookDelivery.getCloudAccountId(),
        webhookDelivery.getDeliveryStatus(),
        webhookDelivery.getResponseCode());
  }
}
