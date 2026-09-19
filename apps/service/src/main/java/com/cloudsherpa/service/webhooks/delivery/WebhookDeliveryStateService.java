package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.lib.entities.WebhookDelivery;
import com.cloudsherpa.lib.entities.WebhookDeliveryStatusEnum;
import com.cloudsherpa.lib.repositories.WebhookDeliveryRepository;
import com.cloudsherpa.service.webhooks.dto.WebhookEventCloudAccount;
import com.cloudsherpa.service.webhooks.model.DeliveryAttempt;
import com.cloudsherpa.service.webhooks.model.DeliveryHeaders;
import com.cloudsherpa.service.webhooks.model.DeliveryTask;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class WebhookDeliveryStateService {
  private final WebhookDeliveryRepository deliveryRepository;

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
  public void recordOutcome(DeliveryTask task, Integer responseCode) {
    WebhookDelivery delivery =
        deliveryRepository
            .findDeliveryForUpdate(task.deliveryId(), WebhookDeliveryStatusEnum.PROCESSING)
            .orElse(null);

    if (delivery == null) {
      return;
    }

    delivery.setAttemptCount(delivery.getAttemptCount() + 1);
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
        new DeliveryHeaders(deliveryId, ""),
        delivery.getWebhook().getEndpointUrl(),
        deliveryId,
        delivery.getEventType(),
        delivery.getEventTimestamp(),
        new WebhookEventCloudAccount(
            delivery.getCloudAccount().getDisplayName(),
            delivery.getCloudAccount().getConnection().getProvider()),
        delivery.getPayload());
  }
}
