package com.cloudsherpa.service.webhooks.queue;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.stereotype.Component;

@Component
public class WebhookEventQueue {

  private static final Integer QUEUE_CAPACITY = 100;
  private final BlockingQueue<UUID> eventQueue = new LinkedBlockingQueue<UUID>(QUEUE_CAPACITY);

  public boolean offerEvent(UUID eventId) {
    return eventQueue.offer(eventId);
  }

  public Integer capacity() {
    return eventQueue.remainingCapacity();
  }
}
