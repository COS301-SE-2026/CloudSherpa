package com.cloudsherpa.service.unit.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.PendingWebhookEvent;
import com.cloudsherpa.lib.entities.Webhook;
import com.cloudsherpa.lib.entities.WebhookDelivery;
import com.cloudsherpa.lib.entities.WebhookDeliveryStatusEnum;
import com.cloudsherpa.lib.entities.WebhookStatusEnum;
import com.cloudsherpa.lib.repositories.PendingWebhookEventRepository;
import com.cloudsherpa.lib.repositories.WebhookDeliveryRepository;
import com.cloudsherpa.lib.repositories.WebhookRepository;
import com.cloudsherpa.service.webhooks.consumers.WebhookEventProcessor;
import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebhookEventProcessorTest {
  @Mock PendingWebhookEventRepository pendingWebhookEventRepository;
  @Mock WebhookRepository webhookRepository;
  @Mock WebhookDeliveryRepository webhookDeliveryRepository;
  @Mock EntityManager entityManager;
  private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
  private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID CLOUD_ACCOUNT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final JsonNode PAYLOAD =
      JsonNodeFactory.instance.objectNode().put("message", "Test Payload");
  private static final Webhook WEBHOOK =
      new Webhook(
          UUID.fromString("00000000-0000-0000-0000-000000000003"),
          "Test Webhook",
          "https://example.com/webhook",
          List.of("dev.event"),
          List.of(),
          WebhookStatusEnum.ACTIVE,
          "Test Signing Key");

  @InjectMocks private WebhookEventProcessor processor;

  @Test
  void returnEmptyListWhenEventNotFound() {
    when(pendingWebhookEventRepository.findByIdForUpdate(EVENT_ID)).thenReturn(Optional.empty());

    List<DeliveryTask> actual = processor.createDeliveries(EVENT_ID);

    assertEquals(List.of(), actual);
  }

  @Test
  void noSubscribersStillDeletesEvent() {
    PendingWebhookEvent event = constructPendingWebhookEvent();
    CloudAccount cloudAccount = mock(CloudAccount.class);
    when(pendingWebhookEventRepository.findByIdForUpdate(EVENT_ID)).thenReturn(Optional.of(event));
    mockNoSubscribedWebhooks();
    mockCloudAccountReference(cloudAccount, CLOUD_ACCOUNT_ID);

    processor.createDeliveries(EVENT_ID);

    verify(pendingWebhookEventRepository).delete(event);
  }

  @Test
  void noSubscribersAddsNoDeliveries() {
    PendingWebhookEvent event = constructPendingWebhookEvent();
    CloudAccount cloudAccount = mock(CloudAccount.class);
    when(pendingWebhookEventRepository.findByIdForUpdate(EVENT_ID)).thenReturn(Optional.of(event));
    mockNoSubscribedWebhooks();
    mockCloudAccountReference(cloudAccount, CLOUD_ACCOUNT_ID);

    List<DeliveryTask> actual = processor.createDeliveries(EVENT_ID);

    assertEquals(List.of(), actual);
    verify(webhookDeliveryRepository, never()).save(any(WebhookDelivery.class));
  }

  @Test
  void deliveryConstruction() {
    PendingWebhookEvent event = constructPendingWebhookEvent();
    CloudAccount cloudAccount = mock(CloudAccount.class);
    when(cloudAccount.getDisplayName()).thenReturn("Test Account");
    when(pendingWebhookEventRepository.findByIdForUpdate(EVENT_ID)).thenReturn(Optional.of(event));
    mockSubscribedWebhooks();
    mockCloudAccountReference(cloudAccount, CLOUD_ACCOUNT_ID);

    List<DeliveryTask> actual = processor.createDeliveries(EVENT_ID);

    ArgumentCaptor<WebhookDelivery> deliveryCaptor = ArgumentCaptor.forClass(WebhookDelivery.class);
    verify(webhookDeliveryRepository).save(deliveryCaptor.capture());
    WebhookDelivery delivery = deliveryCaptor.getValue();

    assertNotNull(delivery.getWebhookDeliveryId());
    assertSame(WEBHOOK, delivery.getWebhook());
    assertEquals(EVENT_ID, delivery.getEventId());
    assertSame(cloudAccount, delivery.getCloudAccount());
    assertEquals("Test Account", delivery.getCoudAccountName());
    assertEquals("dev.event", delivery.getEventType());
    assertEquals(Instant.EPOCH, delivery.getEventTimestamp());
    assertEquals(PAYLOAD, delivery.getPayload());
    assertEquals(WebhookDeliveryStatusEnum.PENDING, delivery.getDeliveryStatus());
    assertEquals(0, delivery.getAttemptCount());
    assertEquals(List.of(new DeliveryTask(TENANT_ID, delivery.getWebhookDeliveryId())), actual);
  }

  @Test
  void ifPendingEventExistsDeletedAfterCreateDeliveries() {
    PendingWebhookEvent event = constructPendingWebhookEvent();
    CloudAccount cloudAccount = mock(CloudAccount.class);
    when(pendingWebhookEventRepository.findByIdForUpdate(EVENT_ID)).thenReturn(Optional.of(event));
    mockSubscribedWebhooks();
    mockCloudAccountReference(cloudAccount, CLOUD_ACCOUNT_ID);

    processor.createDeliveries(EVENT_ID);

    verify(pendingWebhookEventRepository).delete(event);
  }

  private PendingWebhookEvent constructPendingWebhookEvent() {
    return new PendingWebhookEvent(
        EVENT_ID, TENANT_ID, CLOUD_ACCOUNT_ID, "dev.event", Instant.EPOCH, PAYLOAD);
  }

  private void mockCloudAccountReference(CloudAccount cloudAccount, UUID cloudAccountId) {
    when(entityManager.getReference(CloudAccount.class, cloudAccountId)).thenReturn(cloudAccount);
  }

  private void mockSubscribedWebhooks() {
    when(webhookRepository.findSubscribed("dev.event", CLOUD_ACCOUNT_ID))
        .thenReturn(List.of(WEBHOOK));
  }

  private void mockNoSubscribedWebhooks() {
    when(webhookRepository.findSubscribed("dev.event", CLOUD_ACCOUNT_ID)).thenReturn(List.of());
  }
}
