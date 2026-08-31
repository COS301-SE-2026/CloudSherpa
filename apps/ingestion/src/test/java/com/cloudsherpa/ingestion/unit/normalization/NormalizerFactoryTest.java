package com.cloudsherpa.ingestion.unit.normalization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.normalization.normalizers.Normalizer;
import com.cloudsherpa.ingestion.normalization.normalizers.NormalizerFactory;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NormalizerFactoryTest {

  private Map<String, Normalizer> normalizerMap;
  private NormalizerFactory factory;
  private Normalizer awsNormalizer;
  private Normalizer gcpNormalizer;

  @BeforeEach
  void setUp() {
    normalizerMap = new HashMap<>();
    awsNormalizer = mock(Normalizer.class);

    normalizerMap.put("awsNormalizer", awsNormalizer);
    normalizerMap.put("gcpNormalizer", gcpNormalizer);

    factory = new NormalizerFactory(normalizerMap);
  }

  @Test
  void getNormalizerShouldReturnCorrectNormalizerForValidAwsProvider() {
    Normalizer result = factory.getNormalizer("AWS");

    assertNotNull(result);
    assertEquals(awsNormalizer, result);
  }

  @Test
  void getNormalizerShouldReturnCorrectNormalizerForValidGcpProvider() {
    Normalizer result = factory.getNormalizer("GCP");

    assertNotNull(result);
    assertEquals(gcpNormalizer, result);
  }

  @Test
  void getNormalizerShouldHandleMixedCaseProviders() {
    Normalizer result = factory.getNormalizer("aWs");

    assertNotNull(result);
    assertEquals(awsNormalizer, result);
  }

  @Test
  void getNormalizerShouldThrowWhenProviderIsNull() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> factory.getNormalizer(null));

    assertEquals("No provider specified", exception.getMessage());
  }

  @Test
  void getNormalizerShouldThrowWhenNormalizerNotFound() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> factory.getNormalizer("Test"));

    assertEquals("No normalizer found for provider: Test", exception.getMessage());
  }
}
