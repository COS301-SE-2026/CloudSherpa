package com.cloudsherpa.service.analytics.dto;

import com.cloudsherpa.service.analytics.model.ResourceMetric;
import java.util.List;
import java.util.UUID;

public record ResourceMetricsGroupDto(UUID resourceId, List<ResourceMetric> metrics) {}
