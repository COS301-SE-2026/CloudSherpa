package com.cloudsherpa.ingestion.models;

import java.util.Collections;
import java.util.Map;

public class ResourceDetail {

  private final String RESOURCE_ID;
  private final String NAME;
  private final String RESOURCE_TYPE;
  private final Map<String, String> TAGS;

  public ResourceDetail(String resourceId, String name, String type, Map<String, String> tags) {
    this.RESOURCE_ID = resourceId;
    this.NAME = name;
    this.RESOURCE_TYPE = type;
    this.TAGS = tags == null ? Collections.emptyMap() : tags;
  }

  public String getResourceId() {
    return RESOURCE_ID;
  }

  public String getName() {
    return NAME;
  }

  public String getResourceType() {
    return RESOURCE_TYPE;
  }

  public Map<String, String> getTags() {
    return TAGS;
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
