package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
  public record RunInfo(
      @JsonDeserialize(using = AzureManifestDateDeserializer.class) Instant submittedTime,
      String runId,
      @JsonDeserialize(using = AzureManifestDateDeserializer.class) Instant startDate,
      @JsonDeserialize(using = AzureManifestDateDeserializer.class) Instant endDate) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record DeliveryConfig(String fileFormat) {}
}
