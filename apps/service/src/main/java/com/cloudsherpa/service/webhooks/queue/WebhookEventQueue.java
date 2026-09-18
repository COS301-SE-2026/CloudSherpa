package com.cloudsherpa.service.webhooks.queue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.stereotype.Component;

@Component
public class WebhookEventQueue {

  private static final Integer QUEUE_CAPACITY = 100;
  private final BlockingQueue<UUID> eventQueue = new LinkedBlockingQueue<UUID>(QUEUE_CAPACITY);
  // Used to prevent duplicate entries in queue
  private Set<UUID> qeuedOrProcessing = new HashSet<>();

  public boolean offerEvent(UUID eventId) {

    if (qeuedOrProcessing.add(eventId)) {
      if (!eventQueue.add(eventId)) {
        qeuedOrProcessing.remove(eventId);
        return false;
      } else {
        return true;
      }
    }

    return false;
  }

  public Integer capacity() {
    return eventQueue.remainingCapacity();
  }
}
