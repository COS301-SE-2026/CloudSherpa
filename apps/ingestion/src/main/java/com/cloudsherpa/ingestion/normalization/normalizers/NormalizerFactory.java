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
    // Spring names the normalizers as the file name with lowercase
    // This means that if this string is awsNormalizer, then spring does a lookup
    // in the Map with string "awsNormalizer" and gets the AwsNormalizer object as the normalizer
    Normalizer normalizer = normalizers.get(provider.toLowerCase() + "Normalizer");

    if (normalizer == null) {
      throw new IllegalArgumentException("No normalizer found for provider: " + provider);
    }

    return normalizer;
  }
}
