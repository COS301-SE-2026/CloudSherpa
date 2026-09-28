package com.cloudsherpa.service.unit.webhooks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.PendingWebhookEvent;
import com.cloudsherpa.lib.repositories.PendingWebhookEventRepository;
import com.cloudsherpa.service.webhooks.events.devevent.DevPayload;
import com.cloudsherpa.service.webhooks.producers.WebhookProducerService;
import com.cloudsherpa.service.webhooks.queue.WebhookEventQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class WebhookProducerServiceTest {
  @Mock PendingWebhookEventRepository pendingWebhookEventRepository;
  @Mock WebhookEventQueue eventQueue;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  private WebhookProducerService producerService;

  @BeforeEach
  void setUp() {
    producerService =
        new WebhookProducerService(pendingWebhookEventRepository, objectMapper, eventQueue);
  }

  @Test
  void produceEventOffersToQueue() {
    UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID cloudAccountId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    DevPayload payload = new DevPayload("Test Payload", BigDecimal.ZERO, Instant.ofEpochMilli(0));

    producerService.produceEvent(tenantId, cloudAccountId, "dev.event", payload);

    verify(pendingWebhookEventRepository).save(any(PendingWebhookEvent.class));
    verify(eventQueue).offerEvent(any(UUID.class));
  }

  @Test
  void noProductionsRetriedWhenQueueAtCapacity() {
    when(eventQueue.capacity()).thenReturn(0);

    producerService.retryProductions();

    verify(eventQueue, never()).offerEvent(any());
  }

  @Test
  void retryProductionsOffersPendingEvents() {
    UUID eventId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID cloudAccountId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    DevPayload payload = new DevPayload("Test Payload", BigDecimal.ZERO, Instant.ofEpochMilli(0));

    List<PendingWebhookEvent> pendingEvents =
        List.of(constructTestPendingWebhookEvent(eventId, tenantId, cloudAccountId, payload));

    Page<PendingWebhookEvent> pendingEventsPage = new PageImpl<>(pendingEvents);

    when(eventQueue.capacity()).thenReturn(1);
    when(pendingWebhookEventRepository.findAll(
            PageRequest.of(0, 2, Sort.by("eventTimestamp").ascending())))
        .thenReturn(pendingEventsPage);

    producerService.retryProductions();

    verify(eventQueue).offerEvent(eventId);
  }

  private PendingWebhookEvent constructTestPendingWebhookEvent(
      UUID eventId, UUID tenantId, UUID cloudAccountId, DevPayload payload) {
    return new PendingWebhookEvent(
        eventId,
        tenantId,
        cloudAccountId,
        "dev.event",
        Instant.ofEpochMilli(0),
        objectMapper.valueToTree(payload));
  }
}
