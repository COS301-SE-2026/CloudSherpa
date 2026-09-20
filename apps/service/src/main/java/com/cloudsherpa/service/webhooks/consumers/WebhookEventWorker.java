package com.cloudsherpa.service.webhooks.consumers;

import com.cloudsherpa.service.webhooks.delivery.WebhookDeliveryDispatcher;
import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import com.cloudsherpa.service.webhooks.queue.WebhookEventQueue;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class WebhookEventWorker {
  private final WebhookEventQueue queue;
  private final WebhookEventRoutingService router;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final WebhookDeliveryDispatcher dispatcher;
  private final Logger logger = LoggerFactory.getLogger(WebhookEventWorker.class);

  public WebhookEventWorker(
      WebhookEventQueue queue,
      WebhookEventRoutingService router,
      WebhookDeliveryDispatcher dispatcher) {
    this.queue = queue;
    this.router = router;
    this.dispatcher = dispatcher;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void start() {
    executor.execute(this::work);
  }

  private void work() {
    while (!Thread.currentThread().isInterrupted()) {
      UUID eventId;
      try {
        eventId = queue.takeEvent();
        logger.info("Processing event {}", eventId);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }

      try {
        List<DeliveryTask> tasks = router.createDeliveries(eventId);

        for (DeliveryTask task : tasks) {
          logger.info("Submitting delivery task {}", task);
          dispatcher.submit(task);
        }
      } finally {
        queue.finishEvent(eventId);
      }
    }
  }

  @PreDestroy
  public void stop() {
    executor.shutdownNow();
  }
}
