package com.cloudsherpa.service.webhooks.producers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WebhookProducerRetrySchedulor {
  private final WebhookProducerService producerService;

  public WebhookProducerRetrySchedulor(WebhookProducerService producerService) {
    this.producerService = producerService;
  }

  @Scheduled(fixedDelay = 10_000)
  public void retryProductions() {
    producerService.retryProductions();
  }
}
