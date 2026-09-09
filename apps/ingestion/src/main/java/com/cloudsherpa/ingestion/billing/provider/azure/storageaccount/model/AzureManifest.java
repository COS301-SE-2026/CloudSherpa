package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AzureManifest(
    Integer byteCount,
    Integer blobCount,
    Integer dataRowCount,
    RunInfo runInfo,
    DeliveryConfig deliveryConfig,
    List<Partition> blobs) {
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Partition(String blobName, Integer byteCount, Integer dataRowCount) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RunInfo(Instant submittedTime, String runId, Instant startDate, Instant endDate) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record DeliveryConfig(String fileFormat) {}
}
