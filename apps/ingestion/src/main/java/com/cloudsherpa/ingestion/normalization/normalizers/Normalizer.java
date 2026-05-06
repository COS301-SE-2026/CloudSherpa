package com.cloudsherpa.ingestion.normalization.normalizers;

import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import java.util.Map;

public interface Normalizer {
  NormalizedMetric normalize(Map<String, String> rawData);
}
