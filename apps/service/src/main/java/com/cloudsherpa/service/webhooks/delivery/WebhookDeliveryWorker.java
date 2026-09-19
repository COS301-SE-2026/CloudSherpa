package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.service.config.TenantContext;
import com.cloudsherpa.service.resourcediscovery.controller.ResourceDiscoveryController;
import com.cloudsherpa.service.webhooks.model.DeliveryAttempt;
import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WebhookDeliveryWorker {
  private final ResourceDiscoveryController resourceDiscoveryController;
  private final WebhookHttpClient httpClient;
  private final WebhookDeliveryStateService stateService;
  private final Logger logger = LoggerFactory.getLogger(WebhookDeliveryWorker.class);

  public WebhookDeliveryWorker(
      WebhookHttpClient httpClient,
      WebhookDeliveryStateService stateService,
      ResourceDiscoveryController resourceDiscoveryController) {
    this.httpClient = httpClient;
    this.stateService = stateService;
    this.resourceDiscoveryController = resourceDiscoveryController;
  }

  public void process(DeliveryTask task) {
    TenantContext.setCurrentTenant(task.tenantId().toString());
    try {
      DeliveryAttempt attempt = stateService.claim(task);
      logger.info("Attempting delivery {}", attempt);

      if (attempt == null) {
        return;
      }

      Integer responseCode = httpClient.send(attempt);
      logger.info("Received response code {}", responseCode);
      // Record response
    } finally {
      TenantContext.clear();
    }
  }
}
