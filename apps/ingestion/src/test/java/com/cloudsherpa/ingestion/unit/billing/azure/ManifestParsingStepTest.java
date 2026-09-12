package com.cloudsherpa.ingestion.unit.billing.azure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.AzureManifest;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline.ManifestParsingStep;
import com.cloudsherpa.ingestion.provider.azure.services.blobstorage.AzureBlobReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManifestParsingStepTest {

  @Mock AzureBlobReader blobReader;
  @Mock BlobContainerClient blobContainerClient;
  ManifestParsingStep step;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    step = new ManifestParsingStep(objectMapper, blobReader);
  }

  @Test
  void shouldNotThrowWhenNoManifests() {
    AzureBillingContext context = getValidContext();
    context.setManifestBlobItems(List.of());

    step.execute(context);

    assertEquals(0, context.getManifests().size());
  }

  @Test
  void shouldParseValidManifests() {
    AzureBillingContext context = getValidContext();
    context.setManifestBlobItems(List.of(new BlobItem().setName("manifest.json")));

    String manifestJson =
        """
        {
        "manifestVersion": "2024-04-01",
        "byteCount": 8032,
        "blobCount": 1,
        "dataRowCount": 36,
        "exportConfig": {
            "exportName": "sample",
            "resourceId": "/providers/Microsoft.Billing/billingAccounts/1234567/providers/Microsoft.CostManagement/exports/sample",
            "dataVersion": "2023-05-01",
            "apiVersion": "2023-07-01-preview",
            "type": "ReservationRecommendations",
            "timeFrame": "MonthToDate",
            "granularity": null
        },
        "deliveryConfig": {
            "partitionData": true,
            "dataOverwriteBehavior": "OverwritePreviousReport",
            "fileFormat": "Csv",
            "compressionMode": "None",
            "containerUri": "/subscriptions/ 00000000-0000-0000-0000-000000000000/resourceGroups/samplerg/providers/Microsoft.Storage/storageAccounts/samplestorage",
            "rootFolderPath": "folder"
        },
        "runInfo": {
            "executionType": "OnDemand",
            "submittedTime": "2025-03-21T21:04:06.5234447Z",
            "runId": "bbac73f1-9a05-4de6-84ab-c72b568a03b4",
            "startDate": "2025-03-01T00:00:00",
            "endDate": "2025-03-21T00:00:00Z"
        },
        "blobs": [
            {
            "blobName": " folder/sample/ 00000000-0000-0000-0000-000000000000/part0.csv",
            "byteCount": 8032,
            "dataRowCount": 36
            }
        ]
        }
            """;

    mockInputStream(manifestJson, "manifest.json");

    step.execute(context);

    assertEquals(1, context.getManifests().size());
    AzureManifest parsedManifest = context.getManifests().get(0);
    assertEquals(8032, parsedManifest.byteCount());
    assertEquals(1, parsedManifest.blobCount());
    assertEquals(36, parsedManifest.dataRowCount());
    assertEquals(
        " folder/sample/ 00000000-0000-0000-0000-000000000000/part0.csv",
        parsedManifest.blobs().get(0).blobName());
  }

  @Test
  void shouldOnlySkipNotThrowWhenManifestInvalid() {
    AzureBillingContext context = getValidContext();
    context.setManifestBlobItems(List.of(new BlobItem().setName("manifest.json")));

    String manifestJson =
        """
        {
        "manifestVersion": "2024-04-01",
        "byteCount": 8032,
        "blobCount": 1,
        "dataRowCount": 36,
        "exportConfig": {
            "exportName": "sample",
            "resourceId": "/providers/Microsoft.Billing/billingAccounts/1234567/providers/Microsoft.CostManagement/exports/sample",
            "dataVersion": "2023-05-01",
            "apiVersion": "2023-07-01-preview",
            "type": "ReservationRecommendations",
            "timeFrame": "MonthToDate",
            "granularity": null
        },
        }
            """;

    mockInputStream(manifestJson, "manifest.json");

    step.execute(context);

    assertEquals(0, context.getManifests().size());
  }

  @Test
  void shouldOnlySkipNotThrowWhenBlobStorageExceptionThrown() {

    AzureBillingContext context = getValidContext();
    context.setManifestBlobItems(List.of(new BlobItem().setName("manifest.json")));

    HttpResponse response = mock(HttpResponse.class);

    BlobStorageException exception =
        new BlobStorageException(
            "Access denied",
            response,
            "<Error><Code>AuthorizationPermissionMismatch</Code>"
                + "<Message>Access denied</Message></Error>");

    when(blobReader.openStream(blobContainerClient, "manifest.json")).thenThrow(exception);

    step.execute(context);

    assertEquals(0, context.getManifests().size());
  }

  private AzureBillingContext getValidContext() {
    AzureBillingContext context =
        new AzureBillingContext(
            UUID.fromString("00000000-0000-0000-0000-000000000004"),
            UUID.fromString("00000000-0000-0000-0000-000000000001"));

    context.setBlobContainerClient(blobContainerClient);

    return context;
  }

  private void mockInputStream(String json, String blobName) {
    when(blobReader.openStream(blobContainerClient, blobName))
        .thenAnswer(invocation -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
  }
}
