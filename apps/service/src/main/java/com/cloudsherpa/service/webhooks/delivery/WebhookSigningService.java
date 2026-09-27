package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.service.persistconnection.service.CredentialEncryptionService;
import com.cloudsherpa.service.webhooks.model.DeliveryHttpBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class WebhookSigningService {
  private static final String SIGNING_ALGORITHM = "HmacSHA256";

  private final CredentialEncryptionService encryptionService;
  private final ObjectMapper objectMapper;

  public WebhookSigningService(
      CredentialEncryptionService encryptionService, ObjectMapper objectMapper) {
    this.encryptionService = encryptionService;
    this.objectMapper = objectMapper;
  }

  public String signWebhookDelivery(
      String signingKey, String webhookId, Instant timestamp, DeliveryHttpBody body) {

    String key = encryptionService.decrypt(signingKey);
    String message = constructSigningMessage(webhookId, timestamp, body);

    try {
      Mac mac = Mac.getInstance(SIGNING_ALGORITHM);
      SecretKeySpec secretKey =
          new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), SIGNING_ALGORITHM);
      mac.init(secretKey);

      byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(rawHmac);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Webhook signing algorithm is unavailable", e);
    } catch (InvalidKeyException e) {
      throw new IllegalStateException("Webhook signing key is invalid", e);
    }
  }

  private String constructSigningMessage(
      String webhookId, Instant timestamp, DeliveryHttpBody body) {
    try {
      return webhookId + "." + timestamp + "." + objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize webhook delivery for signing", e);
    }
  }
}
