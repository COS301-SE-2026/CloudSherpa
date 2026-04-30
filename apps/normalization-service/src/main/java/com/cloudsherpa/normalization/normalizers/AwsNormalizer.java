package com.cloudsherpa.normalization.normalizers;

import com.cloudsherpa.normalization.model.NormalizedMetric;

public class AwsNormalizer implements Normalizer
{
    @Override
    public NormalizedMetric normalize(String mockRawMetrics)
    {
        return new NormalizedMetric("", "", 0, 0, "", "", "", 0, "", 0, "", "");
    }

    private static String normalizeCategory(String category)
    {
        return ""; // Map the categories from the raw data to our predefined categories that we want to show the user / use
    }

    // Other normalize functions will probably also be added if we see it is necessary
}