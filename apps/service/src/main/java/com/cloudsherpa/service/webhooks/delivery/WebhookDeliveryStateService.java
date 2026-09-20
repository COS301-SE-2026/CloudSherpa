package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.lib.entities.WebhookDelivery;
import com.cloudsherpa.lib.entities.WebhookDeliveryStatusEnum;
import com.cloudsherpa.lib.repositories.WebhookDeliveryRepository;
import com.cloudsherpa.service.webhooks.dto.WebhookEventCloudAccount;
import com.cloudsherpa.service.webhooks.model.DeliveryAttempt;
import com.cloudsherpa.service.webhooks.model.DeliveryHeaders;
import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import jakarta.transaction.Transactional;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WebhookDeliveryStateService {
  private final WebhookDeliveryRepository deliveryRepository;

  private static final Map<Integer, Integer> RETRY_SCHEDULE =
      Map.ofEntries(
          Map.entry(2, 5),
          Map.entry(3, 300),
          Map.entry(4, 1_800),
          Map.entry(5, 7_200),
          Map.entry(6, 18_000),
          Map.entry(7, 36_000),
          Map.entry(8, 50_400),
          Map.entry(9, 72_000));

  public WebhookDeliveryStateService(WebhookDeliveryRepository deliveryRepository) {
    this.deliveryRepository = deliveryRepository;
  }

  @Transactional
  public DeliveryAttempt claim(DeliveryTask task) {
    WebhookDelivery delivery =
        deliveryRepository
            .findDeliveryForUpdate(task.deliveryId(), WebhookDeliveryStatusEnum.PENDING)
            .orElse(null);

    if (delivery == null) {
      return null;
    }
    delivery.setDeliveryStatus(WebhookDeliveryStatusEnum.PROCESSING);
    deliveryRepository.save(delivery);
    return toAttempt(delivery);
  }

  @Transactional
  public void recordOutcome(DeliveryTask task, int responseCode, int attemptsMade) {
    WebhookDelivery delivery =
        deliveryRepository
            .findDeliveryForUpdate(task.deliveryId(), WebhookDeliveryStatusEnum.PROCESSING)
            .orElse(null);

    if (delivery == null) {
      return;
    }

    delivery.setAttemptCount(delivery.getAttemptCount() + attemptsMade);
    delivery.setResponseCode(responseCode);

    if (responseCode >= 200 && responseCode <= 299) {
      delivery.setDeliveryStatus(WebhookDeliveryStatusEnum.DELIVERED);
    } else {
      delivery.setDeliveryStatus(WebhookDeliveryStatusEnum.FAILED);
    }

    deliveryRepository.save(delivery);
  }

  private DeliveryAttempt toAttempt(WebhookDelivery delivery) {

    String deliveryId = "msg_" + delivery.getWebhookDeliveryId().toString().replace("-", "");

    return new DeliveryAttempt(
        new DeliveryHeaders(deliveryId),
        delivery.getWebhook().getEndpointUrl(),
        delivery.getEventType(),
        delivery.getEventTimestamp(),
        new WebhookEventCloudAccount(
            delivery.getCloudAccount().getDisplayName(),
            delivery.getCloudAccount().getConnection().getProvider()),
        delivery.getPayload(),
        delivery.getWebhook().getSigningKey(),
        delivery.getAttemptCount());
  }
}
