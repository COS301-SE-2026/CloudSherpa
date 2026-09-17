package com.cloudsherpa.service.webhooks.service;

import com.cloudsherpa.lib.entities.Webhook;
import com.cloudsherpa.lib.repositories.WebhookRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {
  private final WebhookRepository webhookRepository;

  public WebhookService(WebhookRepository webhookRepository) {
    this.webhookRepository = webhookRepository;
  }

  public List<Webhook> getWebhooks() {
    return List.of();
  }
}
