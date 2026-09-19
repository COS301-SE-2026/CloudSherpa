package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.service.webhooks.model.DeliveryAttempt;
import com.cloudsherpa.service.webhooks.model.DeliveryHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class WebhookHttpClient {
  private final RestClient restClient;

  public WebhookHttpClient(@Qualifier("webhookRestClient") RestClient restClient) {
    this.restClient = restClient;
  }

  // Returns delivery status code
  public Integer send(String webhookUri, DeliveryHeaders headers, DeliveryAttempt attempt) {
    try {
      ResponseEntity<Void> res =
          restClient
              .post()
              .uri(webhookUri)
              .body(attempt)
              .header("Content-Type", "application/json")
              .header("webhook-id", headers.webhookId())
              .header("webhook-timestamp", headers.webhookTimestamp())
              .header("webhook-signature", headers.webhookSignature())
              .retrieve()
              .toBodilessEntity();

      return res.getStatusCode().value();
    } catch (RestClientResponseException e) {
      return e.getStatusCode().value();
    }
  }
}
