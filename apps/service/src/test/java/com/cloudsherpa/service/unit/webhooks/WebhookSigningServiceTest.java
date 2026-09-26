package com.cloudsherpa.service.unit.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.cloudsherpa.service.persistconnection.service.CredentialEncryptionService;
import com.cloudsherpa.service.webhooks.delivery.WebhookSigningService;
import com.cloudsherpa.service.webhooks.dto.WebhookEventCloudAccount;
import com.cloudsherpa.service.webhooks.events.devevent.DevPayload;
import com.cloudsherpa.service.webhooks.model.DeliveryHttpBody;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebhookSigningServiceTest {

  @Mock CredentialEncryptionService encryptionService;
  ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  private WebhookSigningService signingService;

  @BeforeEach
  void setUp() {
    signingService = new WebhookSigningService(encryptionService, objectMapper);
  }

  @Test
  void shouldReturnExpectedSignature() {
    DeliveryHttpBody body = validDeliveryBody();
    when(encryptionService.decrypt("Test Key")).thenReturn("Test Key");

    String expectedSignature = "eb40c84e1c318a0434d25f413514d77d3cccc8e2f32878eaaa0188b5ee4b83ee";

    String actualSignature =
        signingService.signWebhookDelivery(
            "Test Key", "00000000-0000-0000-0000-000000000000", Instant.ofEpochMilli(0), body);

    assertEquals(expectedSignature, actualSignature);
  }

  private DeliveryHttpBody validDeliveryBody() {
    WebhookEventCloudAccount account = new WebhookEventCloudAccount(null, null);
    JsonNode data =
        objectMapper.valueToTree(
            new DevPayload("Test String", BigDecimal.valueOf(0), Instant.ofEpochMilli(0)));
    return new DeliveryHttpBody("dev.event", Instant.ofEpochMilli(0).toEpochMilli(), account, data);
  }
}
