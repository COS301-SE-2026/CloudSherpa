package com.cloudsherpa.ingestion.unit.normalization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.normalization.normalizers.GcpNormalizer;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GcpNormalizerTest {
  private final ResourceRepository resourceRepository = mock(ResourceRepository.class);
  private final GcpNormalizer normalizer = new GcpNormalizer(resourceRepository);

  @Test
  void normalizeShouldReturnNormalizedMetric() {

    UsageRecordModel ingestionRecord = new UsageRecordModel();
    ingestionRecord.setProvider("GCP");
    ingestionRecord.setResourceId("g");
    ingestionRecord.setProjectId(UUID.randomUUID().toString());
    ingestionRecord.setMetricName("CPUUtilization");
    ingestionRecord.setValue(0.75);
    ingestionRecord.setTimestamp(Instant.now());

    NormalizedMetric result = normalizer.normalize(ingestionRecord);

    assertNotNull(result);
  }

  @Test
  void normalizeShouldHandleNullRecord() {

    assertDoesNotThrow(() -> normalizer.normalize(null));
  }

  @Test
  void normalizeShouldHandleMissingFields() {

    UsageRecordModel ingestionRecord = new UsageRecordModel();
    ingestionRecord.setProjectId(UUID.randomUUID().toString());
    ingestionRecord.setResourceId("a");
    assertDoesNotThrow(() -> normalizer.normalize(ingestionRecord));
  }

  @Test
  void normalizeShouldHandlePercentMetric() {
    UsageRecordModel ingestionRecord = new UsageRecordModel();
    ingestionRecord.setUnit("Percent");
    ingestionRecord.setProvider("GCP");
    ingestionRecord.setResourceId("g");
    ingestionRecord.setProjectId(UUID.randomUUID().toString());
    ingestionRecord.setMetricName("CPUUtilization");
    ingestionRecord.setValue(0.5);
    ingestionRecord.setTimestamp(Instant.now());

    NormalizedMetric result = normalizer.normalize(ingestionRecord);
    assertEquals(50, result.getMetricValue());
  }
}
