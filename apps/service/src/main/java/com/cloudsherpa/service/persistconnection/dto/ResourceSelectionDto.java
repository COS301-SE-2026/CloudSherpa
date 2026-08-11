package com.cloudsherpa.service.persistconnection.dto;

import java.util.Map;

public record ResourceSelectionDto(
    String resourceId,
    String serviceType,
    String resourceType,
    String resourceName,
    String region,
    Map<String, Object> tags,
    boolean active) {}
