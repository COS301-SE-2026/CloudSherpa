package com.cloudsherpa.ingestion.normalization.normalizers;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NormalizerFactory {
  private final Map<String, Normalizer> normalizers;

  public NormalizerFactory(Map<String, Normalizer> normalizers) {
    this.normalizers = normalizers;
  }

  public Normalizer getNormalizer(String provider) {
    if (provider == null) {
      throw new IllegalArgumentException("No provider specified");
    }

    // will create awsNormalizer, gcpNormalizer, azureNormalizer
    Normalizer normalizer = normalizers.get(provider + "Normalizer");

    if (normalizer == null) {
      throw new IllegalArgumentException("No normlaizer found for provider: " + provider);
    }

    return normalizer;
  }
}
