package com.cloudsherpa.service.webhooks.queue;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WebhookEventQueue {

  private Logger logger = LoggerFactory.getLogger(WebhookEventQueue.class);

  private static final Integer QUEUE_CAPACITY = 10;
  private final BlockingQueue<UUID> eventQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
  // Used to prevent duplicate entries in queue
  private Set<UUID> qeuedOrProcessing = ConcurrentHashMap.newKeySet();

  public boolean offerEvent(UUID eventId) {

    if (qeuedOrProcessing.add(eventId)) {
      if (!eventQueue.offer(eventId)) {
        qeuedOrProcessing.remove(eventId);
        logger.info("Did not add webhook event {} to the queue", eventId);
        return false;
      } else {
        logger.info("Added webhook event {} to the queue", eventId);
        return true;
      }
    }

    return false;
  }

  public Integer capacity() {
    return eventQueue.remainingCapacity();
  }

  public UUID takeEvent() throws InterruptedException {
    return eventQueue.take();
  }

  public void finishEvent(UUID eventId) {
    qeuedOrProcessing.remove(eventId);
  }
}
