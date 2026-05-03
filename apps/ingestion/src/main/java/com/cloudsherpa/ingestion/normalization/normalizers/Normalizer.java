package com.cloudsherpa.ingestion.normalization.normalizers;

import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;

public interface Normalizer 
{
    // This will eventually be something like NormalizedMetric normalize(RawMetric raw);
    NormalizedMetric normalize(String mockRawMetrics);
}