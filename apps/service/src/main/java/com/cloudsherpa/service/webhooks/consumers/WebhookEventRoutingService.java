package com.cloudsherpa.service.webhooks.consumers;

import com.cloudsherpa.lib.entities.PendingWebhookEvent;
import com.cloudsherpa.lib.repositories.PendingWebhookEventRepository;
import com.cloudsherpa.service.config.TenantContext;
import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WebhookEventRoutingService {
  private final PendingWebhookEventRepository pendingEventRepository;
  private final WebhookEventProcessor processor;

  public WebhookEventRoutingService(
      PendingWebhookEventRepository pendingEventRepository, WebhookEventProcessor processor) {
    this.pendingEventRepository = pendingEventRepository;
    this.processor = processor;
  }

  public List<DeliveryTask> createDeliveries(UUID eventId) {
    try {
      UUID tenantId =
          pendingEventRepository
              .findById(eventId)
              .map(PendingWebhookEvent::getTenantId)
              .orElse(null);

      if (tenantId == null) {
        return List.of();
      }

      TenantContext.setCurrentTenant(tenantId.toString());
      return processor.createDeliveries(eventId);
    } finally {
      TenantContext.clear();
    }
  }
}
