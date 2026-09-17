package com.cloudsherpa.service.webhooks.service;

import com.cloudsherpa.lib.entities.Webhook;
import com.cloudsherpa.lib.repositories.WebhookRepository;
import com.cloudsherpa.service.webhooks.dto.WebhookResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {
  private final WebhookRepository webhookRepository;

  public WebhookService(WebhookRepository webhookRepository) {
    this.webhookRepository = webhookRepository;
  }

  public List<WebhookResponse> getWebhooks() {
    List<Webhook> webhooks = webhookRepository.findAll();
    return webhooks.stream().map(this::fromWebhook).toList();
  }

  private WebhookResponse fromWebhook(Webhook webhook) {
    return new WebhookResponse(null, null, null, null, null, null);
  }
}
