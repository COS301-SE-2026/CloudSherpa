package com.cloudsherpa.service.unit.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.Webhook;
import com.cloudsherpa.lib.entities.WebhookDelivery;
import com.cloudsherpa.lib.entities.WebhookDeliveryStatusEnum;
import com.cloudsherpa.lib.entities.WebhookStatusEnum;
import com.cloudsherpa.lib.repositories.WebhookDeliveryRepository;
import com.cloudsherpa.service.webhooks.delivery.WebhookDeliveryStateService;
import com.cloudsherpa.service.webhooks.dto.WebhookEventCloudAccount;
import com.cloudsherpa.service.webhooks.model.DeliveryAttempt;
import com.cloudsherpa.service.webhooks.model.DeliveryHeaders;
import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebhookDeliveryStateTest {
  @Mock WebhookDeliveryRepository deliveryRepository;

  @InjectMocks WebhookDeliveryStateService stateService;

  private static final DeliveryTask VALID_TASK =
      new DeliveryTask(
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          UUID.fromString("00000000-0000-0000-0000-000000000001"));

  private WebhookDelivery validDelivery;

  @BeforeEach
  void setUp() {
    validDelivery =
        WebhookDelivery.builder()
            .webhookDeliveryId(VALID_TASK.deliveryId())
            .webhook(
                new Webhook(
                    UUID.fromString("00000000-0000-0000-0000-000000000002"),
                    "Test Webhook",
                    "https://example.com/webhook",
                    List.of("dev.event"),
                    List.of(),
                    WebhookStatusEnum.ACTIVE,
                    "Test Signing Key"))
            .eventId(UUID.fromString("00000000-0000-0000-0000-000000000003"))
            .cloudAccountName("Test Account")
            .eventType("dev.event")
            .eventTimestamp(Instant.EPOCH)
            .payload(JsonNodeFactory.instance.objectNode().put("message", "Test Payload"))
            .deliveryStatus(WebhookDeliveryStatusEnum.PENDING)
            .attemptCount(0)
            .build();
  }

  @Test
  void claimReturnsNullWhenNoDeliveryFound() {
    when(deliveryRepository.findDeliveryForUpdate(
            VALID_TASK.deliveryId(), WebhookDeliveryStatusEnum.PENDING))
        .thenReturn(Optional.empty());

    DeliveryAttempt actual = stateService.claim(VALID_TASK);

    assertEquals(null, actual);
  }

  @Test
  void deliveryAttemptContructedFromClaim() {
    DeliveryAttempt expected =
        new DeliveryAttempt(
            new DeliveryHeaders("msg_00000000000000000000000000000001"),
            "https://example.com/webhook",
            "dev.event",
            Instant.EPOCH,
            new WebhookEventCloudAccount("Test Account", null),
            JsonNodeFactory.instance.objectNode().put("message", "Test Payload"),
            "Test Signing Key",
            0);

    when(deliveryRepository.findDeliveryForUpdate(
            VALID_TASK.deliveryId(), WebhookDeliveryStatusEnum.PENDING))
        .thenReturn(Optional.of(validDelivery));

    DeliveryAttempt actual = stateService.claim(VALID_TASK);

    assertEquals(expected, actual);
  }

  @Test
  void recordOutcomeRecordsNothingWhenNoDeliveryFound() {
    when(deliveryRepository.findDeliveryForUpdate(
            VALID_TASK.deliveryId(), WebhookDeliveryStatusEnum.PROCESSING))
        .thenReturn(Optional.empty());

    stateService.recordOutcome(VALID_TASK, 200, 2);

    verify(deliveryRepository, never()).save(any());
  }

  @Test
  void succesfulDeliveryUpdated() {
    when(deliveryRepository.findDeliveryForUpdate(
            VALID_TASK.deliveryId(), WebhookDeliveryStatusEnum.PROCESSING))
        .thenReturn(Optional.of(validDelivery));

    stateService.recordOutcome(VALID_TASK, 200, 2);

    assertEquals(WebhookDeliveryStatusEnum.DELIVERED, validDelivery.getDeliveryStatus());
    assertEquals(2, validDelivery.getAttemptCount());
    assertEquals(null, validDelivery.getNextAttemptAt());
    assertEquals(200, validDelivery.getResponseCode());
    verify(deliveryRepository).save(any());
  }

  @Test
  void failedDeliveryUpdated() {
    when(deliveryRepository.findDeliveryForUpdate(
            VALID_TASK.deliveryId(), WebhookDeliveryStatusEnum.PROCESSING))
        .thenReturn(Optional.of(validDelivery));

    stateService.recordOutcome(VALID_TASK, 500, 2);

    assertEquals(WebhookDeliveryStatusEnum.FAILED, validDelivery.getDeliveryStatus());
    assertEquals(2, validDelivery.getAttemptCount());
    assertNotNull(validDelivery.getNextAttemptAt());
    assertEquals(500, validDelivery.getResponseCode());
    verify(deliveryRepository).save(any());
  }
}
