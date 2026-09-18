package com.cloudsherpa.service.webhooks.queue;

import com.cloudsherpa.service.webhooks.events.WebhookEvent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.stereotype.Component;

@Component
public class WebhookEventQueue {

  private static final Integer QUEUE_CAPACITY = 100;
  private final BlockingQueue<WebhookEvent<?>> eventQueue =
      new LinkedBlockingQueue<WebhookEvent<?>>(QUEUE_CAPACITY);

  public boolean offerEvent(WebhookEvent<?> event) {
    return eventQueue.offer(event);
  }

  public Integer capacity() {
    return eventQueue.remainingCapacity();
  }
}
