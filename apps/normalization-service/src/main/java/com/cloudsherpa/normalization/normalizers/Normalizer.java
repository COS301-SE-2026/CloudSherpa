package com.cloudsherpa.normalization.normalizers;

import com.cloudsherpa.normalization.model.NormalizedMetric;

public interface Normalizer 
{
    // This will eventually be something like NormalizedMetric normalize(RawMetric raw);
    NormalizedMetric normalize(String mockRawMetrics);
}