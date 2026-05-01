package com.cloudsherpa.normalization.normalizers;

import com.cloudsherpa.normalization.model.NormalizedMetric;

public class AwsNormalizer implements Normalizer
{
    @Override
    public NormalizedMetric normalize(String mockRawMetrics)
    {   
        String rawCategory = ""; // mocked raw category
        String normalizedCategory = normalizeCategory(rawCategory);

        return new NormalizedMetric("", "", 0, 0, "", "", "", 0, "", 0, "", "");
    }

    private static String normalizeCategory(String category)
    {
        if (category == null)
        {
            return "other";
        }

        String value = category.toLowerCase();

        if (value.equals("ec2") || value.equals("ecs") || value.equals("eks") || value.equals("lambda"))
        {
            return "compute";
        }

        if (value.equals("s3") || value.equals("ebs") || value.equals("efs"))
        {
            return "storage";
        }

        if (value.equals("rds") || value.equals("dynamodb") || value.equals("aurora"))
        {
            return "database";
        }

        return "other";
    }

    // Other normalize functions will probably also be added if we see it is necessary
}