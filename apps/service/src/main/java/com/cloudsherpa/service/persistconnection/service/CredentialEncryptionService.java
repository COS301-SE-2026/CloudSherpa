package com.cloudsherpa.service.persistconnection.service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CredentialEncryptionService {

  private static final String ALGORITHM = "AES";
  private static final String TRANSFORMATION = "AES/GCM/NoPadding";

  /** Recommended IV (Initialization Vector) length for GCM used : 12 * 8 = 96 bits. */
  private static final int IV_LENGTH = 12;

  /** Authentication tag length in bits. */
  private static final int GCM_TAG_LENGTH_BITS = 128;

  /** AES-256 key length in bytes. */
  private static final int KEY_LENGTH_BYTES = 32;

  /** Minimum encrypted payload = IV + authentication tag. */
  private static final int MIN_ENCRYPTED_LENGTH = IV_LENGTH + (GCM_TAG_LENGTH_BITS / 8);

  private final SecureRandom secureRandom = new SecureRandom();

  @Value("${AES_ENCRYPTION_KEY}")
  private String base64Key;

  private SecretKey secretKey;

  @PostConstruct
  public void init() {
    byte[] decodedKey = Base64.getDecoder().decode(base64Key);

    if (decodedKey.length != KEY_LENGTH_BYTES) {
      throw new IllegalStateException(
          "Encryption key must be a 256-bit (32-byte) Base64-encoded AES key.");
    }

    this.secretKey = new SecretKeySpec(decodedKey, ALGORITHM);
  }

  public String encrypt(String plainText) {
    try {
      byte[] iv = new byte[IV_LENGTH];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

      byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

      byte[] combined = new byte[iv.length + encrypted.length];

      System.arraycopy(iv, 0, combined, 0, iv.length);
      System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

      return Base64.getEncoder().encodeToString(combined);

    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to encrypt data.", e);
    }
  }

  public String decrypt(String encryptedText) {
    try {
      byte[] combined = Base64.getDecoder().decode(encryptedText);

      if (combined.length < MIN_ENCRYPTED_LENGTH) {
        throw new IllegalArgumentException("Invalid encrypted data.");
      }

      byte[] iv = new byte[IV_LENGTH];
      byte[] ciphertext = new byte[combined.length - IV_LENGTH];

      System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
      System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.length);

      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

      byte[] decrypted = cipher.doFinal(ciphertext);

      return new String(decrypted, StandardCharsets.UTF_8);

    } catch (AEADBadTagException e) {
      throw new IllegalArgumentException("Encrypted data is invalid or has been tampered with.", e);
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      throw new IllegalStateException("Failed to decrypt data.", e);
    }
  }
}
