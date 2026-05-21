package com.cloudsherpa.ingestion.unit.normalization;

import static org.junit.jupiter.api.Assertions.*;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.normalization.normalizers.AwsNormalizer;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AwsNormalizerTest {

  private final AwsNormalizer normalizer = new AwsNormalizer();

  @Test
  void normalizeShouldReturnNormalizedMetric() {

    UsageRecordModel record = new UsageRecordModel();
    record.setProvider("AWS");
    record.setMetricName("CPUUtilization");
    record.setValue(75.0);
    record.setTimestamp(Instant.now());

    NormalizedMetric result = normalizer.normalize(record);

    assertNotNull(result);
  }

  @Test
  void normalizeShouldHandleNullRecord() {

    assertDoesNotThrow(() -> normalizer.normalize(null));
  }

  @Test
  void normalizeShouldHandleMissingFields() {

    UsageRecordModel record = new UsageRecordModel();

    assertDoesNotThrow(() -> normalizer.normalize(record));
  }
}
