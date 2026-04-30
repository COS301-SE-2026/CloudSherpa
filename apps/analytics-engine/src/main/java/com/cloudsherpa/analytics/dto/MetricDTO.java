package com.cloudsherpa.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.OffsetDateTime;
import java.util.List;

public class MetricDTO 
{
    private UUID environmentId;
    private String resourceId;
    private String serviceCategory;
    private BigDecimal usageAmount;
    private String usageUnit;
    private BigDecimal costAmount;
    private String currency = "ZAR";

    public MetricDTO()
    {
        // Standard empty constructor 
    }

    public UUID getEnvironmentId() 
    {
        return environmentId;
    }

    public String getResourceId() 
    {
        return resourceId;
    }

    public String getServiceCategory() 
    {
        return serviceCategory;
    }

    public BigDecimal getUsageAmount() 
    {
        return usageAmount;
    }

    public String getUsageUnit() 
    {
        return usageUnit;
    }

    public BigDecimal getCostAmount() 
    {
        return costAmount;
    }

    public String getCurrency() 
    {
        return currency;
    }
}
