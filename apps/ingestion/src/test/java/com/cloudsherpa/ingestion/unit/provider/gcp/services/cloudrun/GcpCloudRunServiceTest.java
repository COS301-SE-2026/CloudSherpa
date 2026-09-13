package com.cloudsherpa.ingestion.unit.provider.gcp.services.cloudrun;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.cloudsherpa.ingestion.provider.gcp.services.cloudrun.GcpCloudRunService;
import com.google.cloud.asset.v1.ResourceSearchResult;
import com.google.cloud.run.v2.GetServiceRequest;
import com.google.cloud.run.v2.ServicesClient;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class GcpCloudRunServiceTest {

  private GcpCloudRunService service;

  private ResourceSearchResult resource;
  private CloudCredentials credentials;
  private ServicesClient client;
  private com.google.cloud.run.v2.Service cloudRunService;

  private static final String ASSET_NAME =
      "//run.googleapis.com/projects/test-project/locations/europe-west1/services/test-service";

  @BeforeEach
  void setUp() {
    service = new GcpCloudRunService();

    resource = mock(ResourceSearchResult.class);
    credentials = mock(CloudCredentials.class);
    client = mock(ServicesClient.class);
    cloudRunService = mock(com.google.cloud.run.v2.Service.class);

    when(resource.getName()).thenReturn(ASSET_NAME);
  }

  @Test
  void getResourceDetail_shouldReturnCorrectResourceDetail() {
    Map<String, String> labels =
        Map.of(
            "environment", "production",
            "team", "platform");

    when(cloudRunService.getName())
        .thenReturn("projects/test-project/locations/europe-west1/services/test-service");
    when(cloudRunService.getLabelsMap()).thenReturn(labels);
    when(client.getService(any(GetServiceRequest.class))).thenReturn(cloudRunService);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createCloudRunClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertNotNull(result);

      assertEquals(
          "projects/test-project/locations/europe-west1/services/test-service",
          result.getResourceId());
      assertEquals("test-service", result.getName());
      assertEquals("service_name", result.getResourceType());
      assertEquals("cloud_run_service", result.getServiceCategory());
      assertEquals("europe-west1", result.getRegion());
      assertEquals(labels, result.getTags());
    }

    verify(client).getService(any(GetServiceRequest.class));
  }

  @Test
  void getResourceDetail_shouldUseProjectLocationAndServiceNameFromAssetName() {
    when(resource.getName())
        .thenReturn(
            "//run.googleapis.com/projects/my-project/locations/us-central1/services/my-service");

    when(cloudRunService.getName())
        .thenReturn("projects/my-project/locations/us-central1/services/my-service");
    when(cloudRunService.getLabelsMap()).thenReturn(Map.of());
    when(client.getService(any(GetServiceRequest.class))).thenReturn(cloudRunService);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createCloudRunClient(credentials)).thenReturn(client);

      service.getResourceDetail(resource, credentials);

      verify(client)
          .getService(
              argThat(
                  (GetServiceRequest request) ->
                      request
                          .getName()
                          .equals(
                              "projects/my-project/locations/us-central1/services/my-service")));
    }
  }

  @Test
  void getResourceDetail_shouldPreserveServiceLabels() {
    Map<String, String> labels =
        Map.of(
            "app", "cloudsherpa",
            "environment", "test",
            "owner", "platform");

    when(cloudRunService.getName())
        .thenReturn("projects/test-project/locations/europe-west1/services/test-service");
    when(cloudRunService.getLabelsMap()).thenReturn(labels);
    when(client.getService(any(GetServiceRequest.class))).thenReturn(cloudRunService);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createCloudRunClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals(labels, result.getTags());
    }
  }

  @Test
  void getResourceDetail_shouldUseServiceNameFromIdentifier() {
    when(cloudRunService.getName())
        .thenReturn("projects/test-project/locations/europe-west1/services/test-service");
    when(cloudRunService.getLabelsMap()).thenReturn(Map.of());
    when(client.getService(any(GetServiceRequest.class))).thenReturn(cloudRunService);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createCloudRunClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals("test-service", result.getName());
    }
  }

  @Test
  void getResourceDetail_shouldUseServiceResourceNameAsResourceId() {
    String serviceResourceName =
        "projects/test-project/locations/europe-west1/services/test-service";

    when(cloudRunService.getName()).thenReturn(serviceResourceName);
    when(cloudRunService.getLabelsMap()).thenReturn(Map.of());
    when(client.getService(any(GetServiceRequest.class))).thenReturn(cloudRunService);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createCloudRunClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals(serviceResourceName, result.getResourceId());
    }
  }

  @Test
  void getResourceDetail_shouldCloseClientAfterSuccessfulLookup() {
    when(cloudRunService.getName())
        .thenReturn("projects/test-project/locations/europe-west1/services/test-service");
    when(cloudRunService.getLabelsMap()).thenReturn(Map.of());
    when(client.getService(any(GetServiceRequest.class))).thenReturn(cloudRunService);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createCloudRunClient(credentials)).thenReturn(client);

      service.getResourceDetail(resource, credentials);
    }

    verify(client).close();
  }

  @Test
  void getResourceDetail_shouldWrapClientException() {
    RuntimeException cause = new RuntimeException("GCP API failure");

    when(client.getService(any(GetServiceRequest.class))).thenThrow(cause);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createCloudRunClient(credentials)).thenReturn(client);

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
      factory.when(() -> GcpClientFactory.createCloudRunClient(credentials)).thenThrow(cause);

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
      factory.when(() -> GcpClientFactory.createCloudRunClient(credentials)).thenThrow(cause);

      assertThrows(
          IllegalStateException.class, () -> service.getResourceDetail(resource, credentials));
    }

    verifyNoInteractions(client);
    verifyNoInteractions(cloudRunService);
  }

  @Test
  void getResourceDetail_shouldPropagateEmptyLabels() {
    when(cloudRunService.getName())
        .thenReturn("projects/test-project/locations/europe-west1/services/test-service");
    when(cloudRunService.getLabelsMap()).thenReturn(Map.of());
    when(client.getService(any(GetServiceRequest.class))).thenReturn(cloudRunService);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createCloudRunClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertNotNull(result);
      assertTrue(result.getTags().isEmpty());
    }
  }
}
