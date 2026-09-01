package com.cloudsherpa.ingestion.unit.scheduling.encryption;

import static org.junit.jupiter.api.Assertions.*;

import com.cloudsherpa.ingestion.scheduler.encryption.CredentialEncryptionService;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CredentialEncryptionServiceTest {

  private CredentialEncryptionService service;

  private static final byte[] KEY = new byte[32];

  @BeforeEach
  void setUp() throws Exception {
    new SecureRandom().nextBytes(KEY);

    service = new CredentialEncryptionService();

    Field secretKeyField = CredentialEncryptionService.class.getDeclaredField("secretKey");

    secretKeyField.setAccessible(true);

    secretKeyField.set(service, new SecretKeySpec(KEY, "AES"));
  }

  @Test
  void encryptThenDecrypt_shouldReturnOriginalPlainText() {
    String original = "{\"accessKey\":\"abc\",\"secretKey\":\"xyz\"}";

    String encrypted = service.encrypt(original);

    String decrypted = service.decrypt(encrypted);

    assertEquals(original, decrypted);
  }

  @Test
  void encrypt_shouldProduceDifferentCiphertextForSamePlainText() {
    String plainText = "sensitive credentials";

    String encrypted1 = service.encrypt(plainText);
    String encrypted2 = service.encrypt(plainText);

    assertNotEquals(encrypted1, encrypted2);
  }

  @Test
  void encryptedValue_shouldNotContainPlainText() {
    String plainText = "my-super-secret";

    String encrypted = service.encrypt(plainText);

    assertFalse(encrypted.contains(plainText));
  }

  @Test
  void decrypt_shouldRejectTooShortPayload() {
    String invalid = Base64.getEncoder().encodeToString(new byte[10]);

    assertThrows(IllegalStateException.class, () -> service.decrypt(invalid));
  }

  @Test
  void decrypt_shouldRejectTamperedCiphertext() {
    String encrypted = service.encrypt("secret");

    byte[] bytes = Base64.getDecoder().decode(encrypted);

    bytes[bytes.length - 1] ^= 1;

    String tampered = Base64.getEncoder().encodeToString(bytes);

    assertThrows(IllegalArgumentException.class, () -> service.decrypt(tampered));
  }

  @Test
  void decrypt_shouldRejectInvalidBase64() {
    assertThrows(IllegalStateException.class, () -> service.decrypt("not-valid-base64!"));
  }
}
