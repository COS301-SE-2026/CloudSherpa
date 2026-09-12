package com.cloudsherpa.ingestion.unit.provider.gcp.services.compute;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.cloudsherpa.ingestion.provider.gcp.services.compute.GcpComputeService;
import com.google.cloud.asset.v1.ResourceSearchResult;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class GcpComputeServiceTest {

  private GcpComputeService service;

  private ResourceSearchResult resource;
  private CloudCredentials credentials;
  private InstancesClient client;
  private Instance instance;

  private static final String ASSET_NAME =
      "//compute.googleapis.com/projects/test-project/zones/europe-west1-b/instances/test-instance";

  @BeforeEach
  void setUp() {
    service = new GcpComputeService();

    resource = mock(ResourceSearchResult.class);
    credentials = mock(CloudCredentials.class);
    client = mock(InstancesClient.class);
    instance = mock(Instance.class);

    when(resource.getName()).thenReturn(ASSET_NAME);
  }

  @Test
  void getResourceDetail_shouldReturnCorrectResourceDetail() {
    Map<String, String> labels =
        Map.of(
            "environment", "production",
            "team", "platform");

    when(instance.getId()).thenReturn(123456789L);
    when(instance.getName()).thenReturn("test-instance");
    when(instance.getLabelsMap()).thenReturn(labels);
    when(client.get("test-project", "europe-west1-b", "test-instance")).thenReturn(instance);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createInstancesClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertNotNull(result);

      assertEquals("123456789", result.getResourceId());
      assertEquals("test-instance", result.getName());
      assertEquals("instance_id", result.getResourceType());
      assertEquals("gce_instance", result.getServiceCategory());
      assertEquals("europe-west1-b", result.getRegion());
      assertEquals(labels, result.getTags());
    }

    verify(client).get("test-project", "europe-west1-b", "test-instance");
  }

  @Test
  void getResourceDetail_shouldUseProjectZoneAndInstanceNameFromAssetName() {
    when(instance.getId()).thenReturn(123L);
    when(instance.getName()).thenReturn("my-instance");
    when(instance.getLabelsMap()).thenReturn(Map.of());

    when(client.get("my-project", "us-central1-a", "my-instance")).thenReturn(instance);

    when(resource.getName())
        .thenReturn(
            "//compute.googleapis.com/projects/my-project/zones/us-central1-a/instances/my-instance");

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createInstancesClient(credentials)).thenReturn(client);

      service.getResourceDetail(resource, credentials);

      verify(client).get("my-project", "us-central1-a", "my-instance");
    }
  }

  @Test
  void getResourceDetail_shouldPreserveInstanceLabels() {
    Map<String, String> labels =
        Map.of(
            "app", "cloudsherpa",
            "environment", "test",
            "owner", "platform");

    when(instance.getId()).thenReturn(42L);
    when(instance.getName()).thenReturn("labelled-instance");
    when(instance.getLabelsMap()).thenReturn(labels);

    when(client.get("test-project", "europe-west1-b", "test-instance")).thenReturn(instance);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createInstancesClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals(labels, result.getTags());
    }
  }

  @Test
  void getResourceDetail_shouldConvertInstanceIdToString() {
    when(instance.getId()).thenReturn(987654321L);
    when(instance.getName()).thenReturn("test-instance");
    when(instance.getLabelsMap()).thenReturn(Map.of());

    when(client.get("test-project", "europe-west1-b", "test-instance")).thenReturn(instance);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createInstancesClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals("987654321", result.getResourceId());
    }
  }

  @Test
  void getResourceDetail_shouldCloseClientAfterSuccessfulLookup() {
    when(instance.getId()).thenReturn(123L);
    when(instance.getName()).thenReturn("test-instance");
    when(instance.getLabelsMap()).thenReturn(Map.of());

    when(client.get("test-project", "europe-west1-b", "test-instance")).thenReturn(instance);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createInstancesClient(credentials)).thenReturn(client);

      service.getResourceDetail(resource, credentials);
    }

    verify(client).close();
  }

  @Test
  void getResourceDetail_shouldWrapClientException() {
    RuntimeException cause = new RuntimeException("GCP API failure");

    when(client.get("test-project", "europe-west1-b", "test-instance")).thenThrow(cause);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createInstancesClient(credentials)).thenReturn(client);

      IllegalStateException exception =
          assertThrows(
              IllegalStateException.class, () -> service.getResourceDetail(resource, credentials));

      assertSame(cause, exception.getCause());
    }

    verify(client).close();
  }

  @Test
  void getResourceDetail_shouldWrapFactoryException() {
    RuntimeException cause = new RuntimeException("Unable to create GCP client");

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createInstancesClient(credentials)).thenThrow(cause);

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
      factory.when(() -> GcpClientFactory.createInstancesClient(credentials)).thenThrow(cause);

      assertThrows(
          IllegalStateException.class, () -> service.getResourceDetail(resource, credentials));
    }

    verifyNoInteractions(client);
    verifyNoInteractions(instance);
  }

  @Test
  void getResourceDetail_shouldPropagateEmptyLabels() {
    when(instance.getId()).thenReturn(123L);
    when(instance.getName()).thenReturn("test-instance");
    when(instance.getLabelsMap()).thenReturn(Map.of());

    when(client.get("test-project", "europe-west1-b", "test-instance")).thenReturn(instance);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createInstancesClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertNotNull(result);
      assertTrue(result.getTags().isEmpty());
    }
  }
}
