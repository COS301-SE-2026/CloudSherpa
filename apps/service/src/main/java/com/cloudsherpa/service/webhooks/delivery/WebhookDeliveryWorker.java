package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.service.config.TenantContext;
import com.cloudsherpa.service.webhooks.model.DeliveryAttempt;
import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WebhookDeliveryWorker {
  private final WebhookHttpClient httpClient;
  private final WebhookDeliveryStateService stateService;
  private final Logger logger = LoggerFactory.getLogger(WebhookDeliveryWorker.class);

  private static final Set<Integer> IMMEDIATE_RETRY_CODES = Set.of(408, 425, 500, 502, 503, 504);

  public WebhookDeliveryWorker(
      WebhookHttpClient httpClient, WebhookDeliveryStateService stateService) {
    this.httpClient = httpClient;
    this.stateService = stateService;
  }

  public void process(DeliveryTask task) {
    TenantContext.setCurrentTenant(task.tenantId().toString());
    try {
      DeliveryAttempt attempt = stateService.claim(task);
      logger.info("Attempting delivery {}", attempt);

      if (attempt == null) {
        return;
      }

      int responseCode = httpClient.send(attempt);
      int attemptsMade = 1;

      // Immediate retry if the first attempt fails
      if (shouldRetryImmediately(responseCode) && attempt.attemptCount() == 0) {
        responseCode = httpClient.send(attempt);
        attemptsMade++;
      }

      stateService.recordOutcome(task, responseCode, attemptsMade);

    } finally {
      TenantContext.clear();
    }
  }

  private boolean shouldRetryImmediately(int responseCode) {
    return responseCode == -1 || IMMEDIATE_RETRY_CODES.contains(responseCode);
  }
}
