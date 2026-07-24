package com.cloudsherpa.service.resourcediscovery.dto;

import java.util.Map;

public record ResourceDetailDto(
    String resourceId,
    String name,
    String resourceType,
    String serviceCategory,
    String region,
    Map<String, String> tags) {
}
