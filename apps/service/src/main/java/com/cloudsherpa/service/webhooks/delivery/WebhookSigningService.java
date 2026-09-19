package com.cloudsherpa.service.webhooks.delivery;

import com.cloudsherpa.service.webhooks.model.DeliveryAttempt;
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

  public String signWebhookDelivery(DeliveryAttempt attempt, Instant timestamp) {

    String key = attempt.signingKey();
    String message =
        attempt.headers().webhookId()
            + attempt.headers()
            + timestamp.toString()
            + attempt.data().toString();

    String algorithm = "HmacSHA256";

    try {
      Mac mac = Mac.getInstance(algorithm);
      SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
      mac.init(secretKey);

      byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(rawHmac);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
  }
}
