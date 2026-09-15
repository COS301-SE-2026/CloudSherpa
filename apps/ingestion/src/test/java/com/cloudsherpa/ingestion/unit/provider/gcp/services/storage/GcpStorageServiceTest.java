package com.cloudsherpa.ingestion.unit.provider.gcp.services.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.cloudsherpa.ingestion.provider.gcp.services.storage.GcpStorageService;
import com.google.cloud.asset.v1.ResourceSearchResult;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class GcpStorageServiceTest {

  private GcpStorageService service;

  private ResourceSearchResult resource;
  private CloudCredentials credentials;
  private Storage client;
  private Bucket bucket;

  private static final String ASSET_NAME = "//storage.googleapis.com/test-bucket";

  @BeforeEach
  void setUp() {
    service = new GcpStorageService();

    resource = mock(ResourceSearchResult.class);
    credentials = mock(CloudCredentials.class);
    client = mock(Storage.class);
    bucket = mock(Bucket.class);

    when(resource.getName()).thenReturn(ASSET_NAME);
  }

  @Test
  void getResourceDetail_shouldReturnCorrectResourceDetail() {
    Map<String, String> labels =
        Map.of(
            "environment", "production",
            "team", "platform");

    when(bucket.getName()).thenReturn("test-bucket");
    when(bucket.getLocation()).thenReturn("EUROPE-WEST1");
    when(bucket.getLabels()).thenReturn(labels);
    when(client.get("test-bucket")).thenReturn(bucket);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createStorageClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertNotNull(result);

      assertEquals("test-bucket", result.getResourceId());
      assertEquals("test-bucket", result.getName());
      assertEquals("bucket_name", result.getResourceType());
      assertEquals("gcs_bucket", result.getServiceCategory());
      assertEquals("EUROPE-WEST1", result.getRegion());
      assertEquals(labels, result.getTags());
    }

    verify(client).get("test-bucket");
  }

  @Test
  void getResourceDetail_shouldUseBucketNameFromAssetName() {
    when(resource.getName()).thenReturn("//storage.googleapis.com/my-test-bucket");

    when(bucket.getName()).thenReturn("my-test-bucket");
    when(bucket.getLocation()).thenReturn("AFRICA-SOUTH1");
    when(bucket.getLabels()).thenReturn(Map.of());
    when(client.get("my-test-bucket")).thenReturn(bucket);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createStorageClient(credentials)).thenReturn(client);

      service.getResourceDetail(resource, credentials);

      verify(client).get("my-test-bucket");
    }
  }

  @Test
  void getResourceDetail_shouldPreserveBucketLabels() {
    Map<String, String> labels =
        Map.of(
            "app", "cloudsherpa",
            "environment", "test",
            "owner", "platform");

    when(bucket.getName()).thenReturn("test-bucket");
    when(bucket.getLocation()).thenReturn("EUROPE-WEST1");
    when(bucket.getLabels()).thenReturn(labels);
    when(client.get("test-bucket")).thenReturn(bucket);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createStorageClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals(labels, result.getTags());
    }
  }

  @Test
  void getResourceDetail_shouldUseBucketNameAsResourceIdAndName() {
    when(bucket.getName()).thenReturn("actual-bucket-name");
    when(bucket.getLocation()).thenReturn("US");
    when(bucket.getLabels()).thenReturn(Map.of());
    when(client.get("test-bucket")).thenReturn(bucket);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createStorageClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals("actual-bucket-name", result.getResourceId());
      assertEquals("actual-bucket-name", result.getName());
    }
  }

  @Test
  void getResourceDetail_shouldUseBucketLocationAsRegion() {
    when(bucket.getName()).thenReturn("test-bucket");
    when(bucket.getLocation()).thenReturn("NORTHAMERICA-NORTHEAST1");
    when(bucket.getLabels()).thenReturn(Map.of());
    when(client.get("test-bucket")).thenReturn(bucket);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createStorageClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals("NORTHAMERICA-NORTHEAST1", result.getRegion());
    }
  }

  @Test
  void getResourceDetail_shouldWrapClientException() {
    RuntimeException cause = new RuntimeException("GCP API failure");

    when(client.get("test-bucket")).thenThrow(cause);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createStorageClient(credentials)).thenReturn(client);

      IllegalStateException exception =
          assertThrows(
              IllegalStateException.class, () -> service.getResourceDetail(resource, credentials));

      assertSame(cause, exception.getCause());
    }
  }

  @Test
  void getResourceDetail_shouldWrapFactoryException() {
    RuntimeException cause = new RuntimeException("Unable to create GCP client");

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createStorageClient(credentials)).thenThrow(cause);

      IllegalStateException exception =
          assertThrows(
              IllegalStateException.class, () -> service.getResourceDetail(resource, credentials));

      assertSame(cause, exception.getCause());
    }

    verifyNoInteractions(client);
  }

  @Test
  void getResourceDetail_shouldNotCallClientWhenFactoryFails() {
    RuntimeException cause = new RuntimeException("Factory failure");

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createStorageClient(credentials)).thenThrow(cause);

      assertThrows(
          IllegalStateException.class, () -> service.getResourceDetail(resource, credentials));
    }

    verifyNoInteractions(client);
    verifyNoInteractions(bucket);
  }

  @Test
  void getResourceDetail_shouldWrapMissingBucket() {
    when(client.get("test-bucket")).thenReturn(null);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createStorageClient(credentials)).thenReturn(client);

      IllegalStateException exception =
          assertThrows(
              IllegalStateException.class, () -> service.getResourceDetail(resource, credentials));

      assertNotNull(exception.getCause());
      assertInstanceOf(IllegalStateException.class, exception.getCause());
    }

    verify(client).get("test-bucket");
  }

  @Test
  void getResourceDetail_shouldPropagateEmptyLabels() {
    when(bucket.getName()).thenReturn("test-bucket");
    when(bucket.getLocation()).thenReturn("EUROPE-WEST1");
    when(bucket.getLabels()).thenReturn(Map.of());
    when(client.get("test-bucket")).thenReturn(bucket);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createStorageClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertNotNull(result);
      assertTrue(result.getTags().isEmpty());
    }
  }
}
