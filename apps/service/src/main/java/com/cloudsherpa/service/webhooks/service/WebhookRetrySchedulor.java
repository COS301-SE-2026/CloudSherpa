package com.cloudsherpa.service.webhooks.service;

import com.cloudsherpa.service.webhooks.producers.WebhookProducerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WebhookRetrySchedulor {
  private final WebhookProducerService producerService;

  public WebhookRetrySchedulor(WebhookProducerService producerService) {
    this.producerService = producerService;
  }

  @Scheduled(fixedDelay = 10_000)
  public void retryProductions() {
    producerService.retryProductions();
  }
}
