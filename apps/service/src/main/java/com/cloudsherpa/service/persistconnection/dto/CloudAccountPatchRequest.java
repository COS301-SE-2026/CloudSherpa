package com.cloudsherpa.service.persistconnection.dto;

public record CloudAccountPatchRequest(
    String displayName,
    Integer ingestionPeriod,
    Boolean periodicResourceDiscovery,
    Boolean autoAdjustIngestionPeriod,
    Boolean newResourcesActive) {

  public boolean hasUpdates() {
    return displayName != null
        || ingestionPeriod != null
        || periodicResourceDiscovery != null
        || autoAdjustIngestionPeriod != null
        || newResourcesActive != null;
  }
}
