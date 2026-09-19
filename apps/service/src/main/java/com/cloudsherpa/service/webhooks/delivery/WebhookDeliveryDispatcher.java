package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class WebhookDeliveryDispatcher {
  private final Logger logger = LoggerFactory.getLogger(WebhookDeliveryDispatcher.class);

  private final ThreadPoolTaskExecutor executor;
  private final WebhookDeliveryWorker worker;

  public WebhookDeliveryDispatcher(
      @Qualifier("webhookDeliveryExecutor") ThreadPoolTaskExecutor executor,
      WebhookDeliveryWorker worker) {
    this.executor = executor;
    this.worker = worker;
  }

  public void submit(DeliveryTask task) {
    try {
      executor.execute(() -> worker.process(task));
    } catch (TaskRejectedException e) {
      logger.debug("Delivery execution deferred for task {}", task);
    }
  }
}
