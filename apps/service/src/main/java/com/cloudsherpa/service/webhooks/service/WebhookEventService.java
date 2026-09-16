package com.cloudsherpa.service.webhooks.service;

import com.cloudsherpa.service.webhooks.events.WebhookEvent;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WebhookEventService {
  public Map<String, WebhookEvent<?>> webhookEventDisplayNames() {
    // Stub
    return Map.of();
  }
}
