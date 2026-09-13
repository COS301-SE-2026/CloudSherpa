package com.cloudsherpa.ingestion.unit.provider.gcp.services.gke;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.cloudsherpa.ingestion.provider.gcp.services.gke.GcpGkeService;
import com.google.cloud.asset.v1.ResourceSearchResult;
import com.google.cloud.container.v1.ClusterManagerClient;
import com.google.container.v1.Cluster;
import com.google.container.v1.GetClusterRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class GcpGkeServiceTest {

  private GcpGkeService service;

  private ResourceSearchResult resource;
  private CloudCredentials credentials;
  private ClusterManagerClient client;
  private Cluster cluster;

  private static final String ASSET_NAME =
      "//container.googleapis.com/projects/test-project/locations/europe-west1/clusters/test-cluster";

  @BeforeEach
  void setUp() {
    service = new GcpGkeService();

    resource = mock(ResourceSearchResult.class);
    credentials = mock(CloudCredentials.class);
    client = mock(ClusterManagerClient.class);
    cluster = mock(Cluster.class);

    when(resource.getName()).thenReturn(ASSET_NAME);
  }

  @Test
  void getResourceDetail_shouldReturnCorrectResourceDetail() {
    Map<String, String> labels =
        Map.of(
            "environment", "production",
            "team", "platform");

    when(cluster.getId()).thenReturn("cluster-id-123");
    when(cluster.getName()).thenReturn("test-cluster");
    when(cluster.getResourceLabelsMap()).thenReturn(labels);
    when(client.getCluster(any(GetClusterRequest.class))).thenReturn(cluster);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createClusterManagerClient(credentials))
          .thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertNotNull(result);

      assertEquals("cluster-id-123", result.getResourceId());
      assertEquals("test-cluster", result.getName());
      assertEquals("cluster_id", result.getResourceType());
      assertEquals("gke_cluster", result.getServiceCategory());
      assertEquals("europe-west1", result.getRegion());
      assertEquals(labels, result.getTags());
    }

    verify(client).getCluster(any(GetClusterRequest.class));
  }

  @Test
  void getResourceDetail_shouldUseProjectLocationAndClusterNameFromAssetName() {
    when(resource.getName())
        .thenReturn(
            "//container.googleapis.com/projects/my-project/locations/us-central1/clusters/my-cluster");

    when(cluster.getId()).thenReturn("cluster-id-456");
    when(cluster.getName()).thenReturn("my-cluster");
    when(cluster.getResourceLabelsMap()).thenReturn(Map.of());
    when(client.getCluster(any(GetClusterRequest.class))).thenReturn(cluster);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createClusterManagerClient(credentials))
          .thenReturn(client);

      service.getResourceDetail(resource, credentials);

      verify(client)
          .getCluster(
              argThat(
                  (GetClusterRequest request) ->
                      request
                          .getName()
                          .equals(
                              "projects/my-project/locations/us-central1/clusters/my-cluster")));
    }
  }

  @Test
  void getResourceDetail_shouldPreserveClusterLabels() {
    Map<String, String> labels =
        Map.of(
            "app", "cloudsherpa",
            "environment", "test",
            "owner", "platform");

    when(cluster.getId()).thenReturn("cluster-id-123");
    when(cluster.getName()).thenReturn("test-cluster");
    when(cluster.getResourceLabelsMap()).thenReturn(labels);
    when(client.getCluster(any(GetClusterRequest.class))).thenReturn(cluster);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createClusterManagerClient(credentials))
          .thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals(labels, result.getTags());
    }
  }

  @Test
  void getResourceDetail_shouldUseClusterNameFromCluster() {
    when(cluster.getId()).thenReturn("cluster-id-123");
    when(cluster.getName()).thenReturn("actual-cluster-name");
    when(cluster.getResourceLabelsMap()).thenReturn(Map.of());
    when(client.getCluster(any(GetClusterRequest.class))).thenReturn(cluster);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createClusterManagerClient(credentials))
          .thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals("actual-cluster-name", result.getName());
    }
  }

  @Test
  void getResourceDetail_shouldUseClusterIdAsResourceId() {
    when(cluster.getId()).thenReturn("cluster-id-789");
    when(cluster.getName()).thenReturn("test-cluster");
    when(cluster.getResourceLabelsMap()).thenReturn(Map.of());
    when(client.getCluster(any(GetClusterRequest.class))).thenReturn(cluster);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createClusterManagerClient(credentials))
          .thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals("cluster-id-789", result.getResourceId());
    }
  }

  @Test
  void getResourceDetail_shouldCloseClientAfterSuccessfulLookup() {
    when(cluster.getId()).thenReturn("cluster-id-123");
    when(cluster.getName()).thenReturn("test-cluster");
    when(cluster.getResourceLabelsMap()).thenReturn(Map.of());
    when(client.getCluster(any(GetClusterRequest.class))).thenReturn(cluster);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createClusterManagerClient(credentials))
          .thenReturn(client);

      service.getResourceDetail(resource, credentials);
    }

    verify(client).close();
  }

  @Test
  void getResourceDetail_shouldWrapClientException() {
    RuntimeException cause = new RuntimeException("GCP API failure");

    when(client.getCluster(any(GetClusterRequest.class))).thenThrow(cause);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createClusterManagerClient(credentials))
          .thenReturn(client);

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
      factory.when(() -> GcpClientFactory.createClusterManagerClient(credentials)).thenThrow(cause);

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
      factory.when(() -> GcpClientFactory.createClusterManagerClient(credentials)).thenThrow(cause);

      assertThrows(
          IllegalStateException.class, () -> service.getResourceDetail(resource, credentials));
    }

    verifyNoInteractions(client);
    verifyNoInteractions(cluster);
  }

  @Test
  void getResourceDetail_shouldPropagateEmptyLabels() {
    when(cluster.getId()).thenReturn("cluster-id-123");
    when(cluster.getName()).thenReturn("test-cluster");
    when(cluster.getResourceLabelsMap()).thenReturn(Map.of());
    when(client.getCluster(any(GetClusterRequest.class))).thenReturn(cluster);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createClusterManagerClient(credentials))
          .thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertNotNull(result);
      assertTrue(result.getTags().isEmpty());
    }
  }
}
