package com.cloudsherpa.normalization.normalizers;

import java.util.Map;

import com.cloudsherpa.normalization.model.NormalizedMetric;

public interface Normalizer 
{
    NormalizedMetric normalize(Map<String, String> row);
}