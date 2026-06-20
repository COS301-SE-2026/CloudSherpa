package com.cloudsherpa.ingestion.models;

import java.util.Collections;
import java.util.Map;

public class ResourceDetail {

  private final String resourceId;
  private final String name;
  private final String resourceType;
  private final String serviceCategory;
  private final Map<String, String> tags;

  public ResourceDetail(
      String resourceId,
      String name,
      String resourceType,
      String serviceCategory,
      Map<String, String> tags) {
    this.resourceId = resourceId;
    this.name = name;
    this.resourceType = resourceType;
    this.serviceCategory = serviceCategory;
    this.tags = tags == null ? Collections.emptyMap() : tags;
  }

  public String getResourceId() {
    return resourceId;
  }

  public String getName() {
    return name;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getServiceCategory() {
    return serviceCategory;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public static String resolveName(
      String resourceId, String explicitName, Map<String, String> tags) {

    if (explicitName != null && !explicitName.isBlank()) {
      return explicitName;
    }

    if (tags != null) {
      String tagName = tags.get("Name");
      if (tagName != null && !tagName.isBlank()) {
        return tagName;
      }
    }

    return resourceId;
  }
}
