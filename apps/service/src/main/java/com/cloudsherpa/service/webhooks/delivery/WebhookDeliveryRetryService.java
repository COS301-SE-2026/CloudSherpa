package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.lib.entities.WebhookDelivery;
import com.cloudsherpa.lib.entities.WebhookDeliveryStatusEnum;
import com.cloudsherpa.lib.repositories.WebhookDeliveryRepository;
import com.cloudsherpa.service.config.TenantContext;
import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class WebhookDeliveryRetryService {
  private final WebhookDeliveryDispatcher dispatcher;
  private final WebhookDeliveryRepository repository;
  private final TransactionTemplate transactionTemplate;

  public WebhookDeliveryRetryService(
      WebhookDeliveryDispatcher dispatcher,
      WebhookDeliveryRepository repository,
      TransactionTemplate transactionTemplate) {
    this.dispatcher = dispatcher;
    this.repository = repository;
    this.transactionTemplate = transactionTemplate;
  }

  public void retryFailedDeliveries(UUID tenantId) {

    List<DeliveryTask> tasks;

    try {
      TenantContext.setCurrentTenant(tenantId.toString());
      tasks = transactionTemplate.execute(status -> claimDueRetryDeliveries(tenantId));
    } finally {
      TenantContext.clear();
    }

    if (tasks != null) {
      tasks.forEach(dispatcher::submit);
    }
  }

  private List<DeliveryTask> claimDueRetryDeliveries(UUID tenantId) {
    List<WebhookDelivery> pendingRetryDeliveries =
        repository.findDeliveriesForRetry(WebhookDeliveryStatusEnum.FAILED, PageRequest.of(0, 100));

    List<DeliveryTask> deliveryTasks = new ArrayList<>();

    for (WebhookDelivery delivery : pendingRetryDeliveries) {
      delivery.setDeliveryStatus(WebhookDeliveryStatusEnum.PENDING);
      repository.save(delivery);
      deliveryTasks.add(new DeliveryTask(tenantId, delivery.getWebhookDeliveryId()));
    }

    return deliveryTasks;
  }
}
