package com.cloudsherpa.ingestion.unit.normalization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.cloudsherpa.ingestion.normalization.normalizers.GcpNormalizer;
import com.cloudsherpa.lib.repositories.ResourceRepository;

public class GcpNormalizerTest {
  private final ResourceRepository resourceRepository = mock(ResourceRepository.class);
  private final GcpNormalizer normalizer = new GcpNormalizer(resourceRepository);
}
