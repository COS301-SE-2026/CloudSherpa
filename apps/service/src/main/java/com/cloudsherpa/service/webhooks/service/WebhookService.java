package com.cloudsherpa.service.webhooks.service;

import com.cloudsherpa.lib.entities.Webhook;
import com.cloudsherpa.lib.entities.WebhookDelivery;
import com.cloudsherpa.lib.entities.WebhookStatusEnum;
import com.cloudsherpa.lib.repositories.WebhookDeliveryRepository;
import com.cloudsherpa.lib.repositories.WebhookRepository;
import com.cloudsherpa.service.webhooks.dto.AddWebhookDto;
import com.cloudsherpa.service.webhooks.dto.AddWebhookResponseDto;
import com.cloudsherpa.service.webhooks.dto.EditWebhookDto;
import com.cloudsherpa.service.webhooks.dto.WebhookDeliveryResponse;
import com.cloudsherpa.service.webhooks.dto.WebhookResponse;
import com.cloudsherpa.service.webhooks.exceptions.WebhookNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookService {
  private final WebhookRepository webhookRepository;
  private final WebhookDeliveryRepository webhookDeliveryRepository;
  private final Logger logger = LoggerFactory.getLogger(WebhookService.class);

  public WebhookService(
      WebhookRepository webhookRepository, WebhookDeliveryRepository webhookDeliveryRepository) {
    this.webhookRepository = webhookRepository;
    this.webhookDeliveryRepository = webhookDeliveryRepository;
  }

  public List<WebhookResponse> getWebhooks() {
    List<Webhook> webhooks = webhookRepository.findAll();
    return webhooks.stream().map(this::fromWebhook).toList();
  }

  // Need to be part of transaction since webhook & cloud account entities loaded lazily
  @Transactional(readOnly = true)
  public List<WebhookDeliveryResponse> getWebhookDeliveries() {
    List<WebhookDelivery> webhookDeliveries = webhookDeliveryRepository.findAll();
    return webhookDeliveries.stream().map(this::fromWebhookDelivery).toList();
  }

  public AddWebhookResponseDto addWebhook(AddWebhookDto request) {

    String webhookKey = generateHmacSigningSecret();

    Webhook webhook =
        new Webhook(
            UUID.randomUUID(),
            request.name(),
            request.endpointUrl(),
            request.eventTypes(),
            request.cloudAccounts(),
            WebhookStatusEnum.ACTIVE,
            webhookKey);
    webhookRepository.save(webhook);

    return new AddWebhookResponseDto(webhookKey);
  }

  public void editWebhook(UUID webhookId, EditWebhookDto request) {
    try {
      Webhook webhook = webhookRepository.findById(webhookId).orElseThrow();

      webhook.setWebhookName(request.name());
      webhook.setEndpointUrl(request.endpointUrl());
      webhook.setEventTypes(request.eventTypes());
      webhook.setWebhookStatus(request.status());
      webhook.setCloudAccounts(request.cloudAccounts());

      webhookRepository.save(webhook);
    } catch (NoSuchElementException e) {
      throw new WebhookNotFoundException(webhookId);
    }
  }

  public void deleteWebhook(UUID webhookId) {
    try {
      webhookRepository.deleteAllById(List.of(webhookId));
    } catch (IllegalArgumentException e) {
      logger.warn("Webhook with ID {} was not found and hence already absent", webhookId);
    }
  }

  private WebhookResponse fromWebhook(Webhook webhook) {
    return new WebhookResponse(
        webhook.getWebhookId(),
        webhook.getWebhookName(),
        webhook.getEndpointUrl(),
        webhook.getEventTypes(),
        webhook.getCloudAccounts(),
        webhook.getWebhookStatus());
  }

  private WebhookDeliveryResponse fromWebhookDelivery(WebhookDelivery webhookDelivery) {
    return new WebhookDeliveryResponse(
        webhookDelivery.getWebhookDeliveryId(),
        webhookDelivery.getEventTimestamp(),
        webhookDelivery.getWebhook() != null ? webhookDelivery.getWebhook().getWebhookId() : null,
        webhookDelivery.getEventType(),
        webhookDelivery.getCloudAccount().getId(),
        webhookDelivery.getDeliveryStatus(),
        webhookDelivery.getResponseCode());
  }

  private String generateHmacSigningSecret() {
    try {
      KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
      SecretKey secretKey = keyGen.generateKey();

      return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(
          "Algorithm for generating webhook HMAC keys does not exist", e);
    }
  }
}
