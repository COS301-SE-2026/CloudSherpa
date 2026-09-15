package com.cloudsherpa.ingestion.unit.provider.gcp.services.gar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.cloudsherpa.ingestion.provider.gcp.services.gar.GcpGarService;
import com.google.cloud.asset.v1.ResourceSearchResult;
import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient;
import com.google.devtools.artifactregistry.v1.GetRepositoryRequest;
import com.google.devtools.artifactregistry.v1.Repository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class GcpGarServiceTest {

  private GcpGarService service;

  private ResourceSearchResult resource;
  private CloudCredentials credentials;
  private ArtifactRegistryClient client;
  private Repository repository;

  private static final String ASSET_NAME =
      "//artifactregistry.googleapis.com/projects/test-project/locations/europe-west1/repositories/test-repository";

  @BeforeEach
  void setUp() {
    service = new GcpGarService();

    resource = mock(ResourceSearchResult.class);
    credentials = mock(CloudCredentials.class);
    client = mock(ArtifactRegistryClient.class);
    repository = mock(Repository.class);

    when(resource.getName()).thenReturn(ASSET_NAME);
  }

  @Test
  void getResourceDetail_shouldReturnCorrectResourceDetail() {
    Map<String, String> labels =
        Map.of(
            "environment", "production",
            "team", "platform");

    when(repository.getName())
        .thenReturn("projects/test-project/locations/europe-west1/repositories/test-repository");
    when(repository.getLabelsMap()).thenReturn(labels);
    when(client.getRepository(any(GetRepositoryRequest.class))).thenReturn(repository);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createArtifactRegistryClient(credentials))
          .thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertNotNull(result);

      assertEquals("test-repository", result.getResourceId());
      assertEquals("test-repository", result.getName());
      assertEquals("repository_id", result.getResourceType());
      assertEquals("artifactregistry.googleapis.com/Repository", result.getServiceCategory());
      assertEquals("europe-west1", result.getRegion());
      assertEquals(labels, result.getTags());
    }

    verify(client).getRepository(any(GetRepositoryRequest.class));
  }

  @Test
  void getResourceDetail_shouldUseProjectLocationAndRepositoryNameFromAssetName() {
    when(resource.getName())
        .thenReturn(
            "//artifactregistry.googleapis.com/projects/my-project/locations/us-central1/repositories/my-repository");

    when(repository.getName())
        .thenReturn("projects/my-project/locations/us-central1/repositories/my-repository");
    when(repository.getLabelsMap()).thenReturn(Map.of());
    when(client.getRepository(any(GetRepositoryRequest.class))).thenReturn(repository);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createArtifactRegistryClient(credentials))
          .thenReturn(client);

      service.getResourceDetail(resource, credentials);

      verify(client)
          .getRepository(
              argThat(
                  (GetRepositoryRequest request) ->
                      request
                          .getName()
                          .equals(
                              "projects/my-project/locations/us-central1/repositories/my-repository")));
    }
  }

  @Test
  void getResourceDetail_shouldPreserveRepositoryLabels() {
    Map<String, String> labels =
        Map.of(
            "app", "cloudsherpa",
            "environment", "test",
            "owner", "platform");

    when(repository.getName())
        .thenReturn("projects/test-project/locations/europe-west1/repositories/test-repository");
    when(repository.getLabelsMap()).thenReturn(labels);
    when(client.getRepository(any(GetRepositoryRequest.class))).thenReturn(repository);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createArtifactRegistryClient(credentials))
          .thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals(labels, result.getTags());
    }
  }

  @Test
  void getResourceDetail_shouldUseRepositoryNameFromIdentifier() {
    when(repository.getName())
        .thenReturn("projects/test-project/locations/europe-west1/repositories/test-repository");
    when(repository.getLabelsMap()).thenReturn(Map.of());
    when(client.getRepository(any(GetRepositoryRequest.class))).thenReturn(repository);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createArtifactRegistryClient(credentials))
          .thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals("test-repository", result.getName());
    }
  }

  @Test
  void getResourceDetail_shouldUseRepositoryResourceNameAsResourceId() {
    String repositoryResourceName = "test-repository";

    when(repository.getName()).thenReturn(repositoryResourceName);
    when(repository.getLabelsMap()).thenReturn(Map.of());
    when(client.getRepository(any(GetRepositoryRequest.class))).thenReturn(repository);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createArtifactRegistryClient(credentials))
          .thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals(repositoryResourceName, result.getResourceId());
    }
  }

  @Test
  void getResourceDetail_shouldCloseClientAfterSuccessfulLookup() {
    when(repository.getName())
        .thenReturn("projects/test-project/locations/europe-west1/repositories/test-repository");
    when(repository.getLabelsMap()).thenReturn(Map.of());
    when(client.getRepository(any(GetRepositoryRequest.class))).thenReturn(repository);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createArtifactRegistryClient(credentials))
          .thenReturn(client);

      service.getResourceDetail(resource, credentials);
    }

    verify(client).close();
  }

  @Test
  void getResourceDetail_shouldWrapClientException() {
    RuntimeException cause = new RuntimeException("GCP API failure");

    when(client.getRepository(any(GetRepositoryRequest.class))).thenThrow(cause);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createArtifactRegistryClient(credentials))
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
      factory
          .when(() -> GcpClientFactory.createArtifactRegistryClient(credentials))
          .thenThrow(cause);

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
      factory
          .when(() -> GcpClientFactory.createArtifactRegistryClient(credentials))
          .thenThrow(cause);

      assertThrows(
          IllegalStateException.class, () -> service.getResourceDetail(resource, credentials));
    }

    verifyNoInteractions(client);
    verifyNoInteractions(repository);
  }

  @Test
  void getResourceDetail_shouldPropagateEmptyLabels() {
    when(repository.getName())
        .thenReturn("projects/test-project/locations/europe-west1/repositories/test-repository");
    when(repository.getLabelsMap()).thenReturn(Map.of());
    when(client.getRepository(any(GetRepositoryRequest.class))).thenReturn(repository);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory
          .when(() -> GcpClientFactory.createArtifactRegistryClient(credentials))
          .thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertNotNull(result);
      assertTrue(result.getTags().isEmpty());
    }
  }
}
