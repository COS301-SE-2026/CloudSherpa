package com.cloudsherpa.service.unit.webhooks;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.service.webhooks.consumers.WebhookEventRoutingService;
import com.cloudsherpa.service.webhooks.consumers.WebhookEventWorker;
import com.cloudsherpa.service.webhooks.delivery.WebhookDeliveryDispatcher;
import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import com.cloudsherpa.service.webhooks.queue.WebhookEventQueue;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebhookEventWorkerTest {
  @Mock WebhookEventQueue queue;
  @Mock WebhookEventRoutingService router;
  @Mock WebhookDeliveryDispatcher dispatcher;

  @InjectMocks private WebhookEventWorker worker;

  @AfterEach
  void tearDown() {
    worker.stop();
  }

  @Test
  void processesEventAndSubmitsDelivery() throws InterruptedException {
    UUID eventId = UUID.randomUUID();
    DeliveryTask deliveryTask = new DeliveryTask(UUID.randomUUID(), UUID.randomUUID());
    when(queue.takeEvent()).thenReturn(eventId).thenThrow(new InterruptedException());
    when(router.createDeliveries(eventId)).thenReturn(List.of(deliveryTask));

    worker.start();

    verify(router, timeout(1_000)).createDeliveries(eventId);
    verify(dispatcher, timeout(1_000)).submit(deliveryTask);
    verify(queue, timeout(1_000)).finishEvent(eventId);
  }
}
