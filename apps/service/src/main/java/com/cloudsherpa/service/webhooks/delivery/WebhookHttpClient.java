package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.service.webhooks.model.DeliveryAttempt;
import com.cloudsherpa.service.webhooks.model.DeliveryHttpBody;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class WebhookHttpClient {
  private final RestClient restClient;
  private final WebhookSigningService signingService;

  public WebhookHttpClient(
      @Qualifier("webhookRestClient") RestClient restClient, WebhookSigningService signingService) {
    this.restClient = restClient;
    this.signingService = signingService;
  }

  // Returns delivery status code
  public Integer send(DeliveryAttempt attempt) {
    try {
      Instant timestamp = Instant.now();
      ResponseEntity<Void> res =
          restClient
              .post()
              .uri(attempt.webhookUrl())
              .body(requestBody(attempt))
              .header("Content-Type", "application/json")
              .header("webhook-id", attempt.headers().webhookId())
              .header("webhook-timestamp", timestamp.toString())
              .header("webhook-signature", signingService.signWebhookDelivery(attempt, timestamp))
              .retrieve()
              .toBodilessEntity();

      return res.getStatusCode().value();
    } catch (RestClientResponseException e) {
      return e.getStatusCode().value();
    } catch (ResourceAccessException e) {
      return -1;
    }
  }

  private DeliveryHttpBody requestBody(DeliveryAttempt attempt) {
    return new DeliveryHttpBody(
        attempt.type(), attempt.timestamp().toString(), attempt.account(), attempt.data());
  }
}
