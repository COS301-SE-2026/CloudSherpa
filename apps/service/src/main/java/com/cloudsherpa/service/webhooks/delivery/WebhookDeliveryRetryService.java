package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.lib.repositories.WebhookDeliveryRepository;
import com.cloudsherpa.service.config.TenantContext;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WebhookDeliveryRetryService {
  private final WebhookDeliveryDispatcher dispatcher;
  private final WebhookDeliveryRepository repository;

  public WebhookDeliveryRetryService(
      WebhookDeliveryDispatcher dispatcher, WebhookDeliveryRepository repository) {
    this.dispatcher = dispatcher;
    this.repository = repository;
  }

  public void retryFailedDeliveries(UUID tenantId) {
    TenantContext.setCurrentTenant(tenantId.toString());

    try {
      // something
    } finally {
      TenantContext.clear();
    }
  }
}
