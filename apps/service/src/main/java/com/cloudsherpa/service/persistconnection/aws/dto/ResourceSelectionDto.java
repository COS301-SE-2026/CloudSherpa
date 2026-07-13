package com.cloudsherpa.service.persistconnection.aws.dto;

import java.util.Map;

public record ResourceSelectionDto(
    String resourceId,
    String resourceType,
    String resourceName,
    String region,
    Map<String, Object> tags,
    boolean active) {}
