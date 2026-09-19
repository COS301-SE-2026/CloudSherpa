package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

@Service
public class WebhookDeliveryDispatcher {
  private final Logger logger = LoggerFactory.getLogger(WebhookDeliveryDispatcher.class);

  private final ThreadPoolExecutor executor;
  private final WebhookDeliveryWorker worker;

  public WebhookDeliveryDispatcher(ThreadPoolExecutor executor, WebhookDeliveryWorker worker) {
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
