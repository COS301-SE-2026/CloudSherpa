package com.cloudsherpa.normalization.normalizers;

import com.cloudsherpa.normalization.model.NormalizedMetric;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public class AwsNormalizer implements Normalizer
{
    @Override
    public NormalizedMetric normalize(Map<String, String> row)
    {   
        if (row == null || row.isEmpty())
        {
            return null; // validation needs to happen in the class that creates the normalized metrics
        }

        String metricId = UUID.randomUUID().toString();
        String provider = "AWS";

        long usageStart = parseTime(row.get("lineItem_UsageStartDate"));
        long usageEnd = parseTime(row.get("lineItem_UsageEndDate"));

        String resourceId = row.get("lineItem_ResourceId");

        String service = row.get("lineItem_ProductCode");

        String serviceCategory = normalizeCategory(service);

        double usageAmount = parseDouble(row.get("lineItem_UsageAmount"));

        String usageUnit = row.get("pricing_unit");

        double effectiveCost = parseDouble(row.get("lineItem_UnblendedCost"));

        String currency = "ZAR"; // We will need to determine what currency it is in and convert it to ZAR

        String pricingModel = normalizePricingModel(row.get("lineItem_LineItemType"));

        return new NormalizedMetric(
                metricId,
                provider,
                usageStart,
                usageEnd,
                resourceId,
                service,
                serviceCategory,
                usageAmount,
                usageUnit,
                effectiveCost,
                currency,
                pricingModel
        );
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

    private long parseTime(String value)
    {
        if (value == null) 
        {
            return 0;
        }

        // Converts to Unix timestamp in milliseconds
        // converts input from 2023-01-01T12:00:00Z
        // to 1672574400000 which is standard for timestamps in Java 
        return OffsetDateTime.parse(value).toEpochSecond() * 1000;
    }

    private double parseDouble(String value)
    {
        if (value == null || value.isEmpty()) 
        {
            return 0.0;
        }

        return Double.parseDouble(value);
    }

    private String normalizePricingModel(String model)
    {
        if (model == null) 
        {
            return "on_demand";
        }

        if (model.contains("SavingsPlan")) 
        {
            return "savings_plan";
        }

        if (model.contains("Reserved")) 
        {
            return "reserved";
        }

        return "on_demand";
    }
}