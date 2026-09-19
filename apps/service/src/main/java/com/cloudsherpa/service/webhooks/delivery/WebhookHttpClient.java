package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.service.webhooks.model.DeliveryAttempt;
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

  public WebhookHttpClient(@Qualifier("webhookRestClient") RestClient restClient) {
    this.restClient = restClient;
  }

  // Returns delivery status code
  public Integer send(DeliveryAttempt attempt) {
    try {
      ResponseEntity<Void> res =
          restClient
              .post()
              .uri(attempt.webhookUrl())
              .body(attempt)
              .header("Content-Type", "application/json")
              .header("webhook-id", attempt.headers().webhookId())
              .header("webhook-timestamp", Instant.now().toString())
              .header("webhook-signature", attempt.headers().webhookSignature())
              .retrieve()
              .toBodilessEntity();

      return res.getStatusCode().value();
    } catch (RestClientResponseException e) {
      return e.getStatusCode().value();
    } catch (ResourceAccessException e) {
      return -1;
    }
  }
}
