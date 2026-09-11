package com.cloudsherpa.ingestion.unit.billing.azure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline.ManifestDiscoveryStep;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.provider.azure.factory.AzureClientFactory;
import com.cloudsherpa.lib.entities.AzureBillingExportConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManifestDiscoveryStepTest {

  @Mock AzureClientFactory azureClientFactory;
  @Mock BlobServiceClient blobServiceClient;
  @Mock BlobContainerClient blobContainerClient;

  ManifestDiscoveryStep step;

  @BeforeEach
  void setUp() {
    this.step = new ManifestDiscoveryStep();
  }

  @Test
  void getBlobEndpointShouldReturnValidEndpoint() {
    AzureBillingContext context = getValidContext();
    mockAzureClients(context);
    mockBlobList();

    String endpoint = "https://teststorageaccount.blob.core.windows.net";

    try (MockedStatic<AzureClientFactory> factory = Mockito.mockStatic(AzureClientFactory.class)) {
      factory
          .when(
              () -> AzureClientFactory.createBlobServiceClient(context.getCredentials(), endpoint))
          .thenReturn(blobServiceClient);

      step.execute(context);

      factory.verify(
          () -> AzureClientFactory.createBlobServiceClient(context.getCredentials(), endpoint));
    }
  }

  @Test
  void getExportPathShouldReturnValidPath() {
    AzureBillingContext context = getValidContext();
    mockAzureClients(context);
    mockBlobList();

    executeWithMockedFactory(context);

    ArgumentCaptor<ListBlobsOptions> captor = ArgumentCaptor.forClass(ListBlobsOptions.class);

    verify(blobContainerClient).listBlobs(captor.capture(), isNull());

    assertEquals("exports/daily/test-billing-export/", captor.getValue().getPrefix());
  }

  @Test
  void blobItemsShouldBeAddedToContext() {
    AzureBillingContext context = getValidContext();
    mockAzureClients(context);
    mockBlobList();

    executeWithMockedFactory(context);

    assertEquals(1, context.getManifestBlobItems().size());
    assertEquals(
        List.of("billing/run1/manifest.json"),
        context.getManifestBlobItems().stream().map(BlobItem::getName).toList());
  }

  @Test
  void blobListingFailureShouldThrow() {
    AzureBillingContext context = getValidContext();
    mockAzureClients(context);

    HttpResponse response = mock(HttpResponse.class);
    when(response.getStatusCode()).thenReturn(403);

    HttpHeaders headers = mock(HttpHeaders.class);
    when(response.getHeaders()).thenReturn(headers);

    BlobStorageException exception =
        new BlobStorageException(
            "Access denied",
            response,
            "<Error><Code>AuthorizationPermissionMismatch</Code>"
                + "<Message>Access denied</Message></Error>");

    when(blobContainerClient.listBlobs(any(ListBlobsOptions.class), isNull())).thenThrow(exception);

    assertThrows(IllegalStateException.class, () -> executeWithMockedFactory(context));
  }

  @Test
  void clientAuthExceptionShouldThrow() {
    AzureBillingContext context = getValidContext();
    mockAzureClients(context);

    HttpResponse response = mock(HttpResponse.class);
    ClientAuthenticationException exception =
        new ClientAuthenticationException(
            "Failed to acquire an Azure access token using the configured credentials.", response);

    when(blobContainerClient.listBlobs(any(ListBlobsOptions.class), isNull())).thenThrow(exception);

    assertThrows(IllegalStateException.class, () -> executeWithMockedFactory(context));
  }

  private void executeWithMockedFactory(AzureBillingContext context) {
    String endpoint = "https://teststorageaccount.blob.core.windows.net";

    try (MockedStatic<AzureClientFactory> factory = Mockito.mockStatic(AzureClientFactory.class)) {
      factory
          .when(
              () -> AzureClientFactory.createBlobServiceClient(context.getCredentials(), endpoint))
          .thenReturn(blobServiceClient);

      step.execute(context);
    }
  }

  private AzureBillingContext getValidContext() {
    AzureBillingContext context =
        new AzureBillingContext(
            UUID.fromString("00000000-0000-0000-0000-000000000004"),
            UUID.fromString("00000000-0000-0000-0000-000000000001"));
    context.setCredentials(new CloudCredentials());
    context.setExportConfig(getValidAzureBillingExportConfig());

    return context;
  }

  private AzureBillingExportConfig getValidAzureBillingExportConfig() {
    return new AzureBillingExportConfig(
        UUID.fromString("00000000-0000-0000-0000-000000000001"),
        "teststorageaccount",
        "billing-exports",
        "exports/daily",
        "test-billing-export");
  }

  private void mockAzureClients(AzureBillingContext context) {
    when(blobServiceClient.getBlobContainerClient(context.getExportConfig().getStorageContainer()))
        .thenReturn(blobContainerClient);
  }

  private void mockBlobList() {
    List<BlobItem> blobs =
        List.of(
            new BlobItem().setName("billing/run1/manifest.json"),
            new BlobItem().setName("billing/run1/part0.csv"));

    PagedResponse<BlobItem> page =
        new PagedResponseBase<Void, BlobItem>(null, 200, new HttpHeaders(), blobs, null, null);

    PagedIterable<BlobItem> result = new PagedIterable<>(() -> page);

    when(blobContainerClient.listBlobs(any(ListBlobsOptions.class), isNull())).thenReturn(result);
  }
}
