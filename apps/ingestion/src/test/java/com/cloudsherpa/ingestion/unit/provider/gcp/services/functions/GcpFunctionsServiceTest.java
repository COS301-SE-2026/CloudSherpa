package com.cloudsherpa.ingestion.unit.provider.gcp.services.functions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.cloudsherpa.ingestion.provider.gcp.services.functions.GcpFunctionsService;
import com.google.cloud.asset.v1.ResourceSearchResult;
import com.google.cloud.functions.v2.Function;
import com.google.cloud.functions.v2.FunctionServiceClient;
import com.google.cloud.functions.v2.GetFunctionRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class GcpFunctionsServiceTest {

  private GcpFunctionsService service;

  private ResourceSearchResult resource;
  private CloudCredentials credentials;
  private FunctionServiceClient client;
  private Function function;

  private static final String ASSET_NAME =
      "//cloudfunctions.googleapis.com/projects/test-project/locations/europe-west1/functions/test-function";

  @BeforeEach
  void setUp() {
    service = new GcpFunctionsService();

    resource = mock(ResourceSearchResult.class);
    credentials = mock(CloudCredentials.class);
    client = mock(FunctionServiceClient.class);
    function = mock(Function.class);

    when(resource.getName()).thenReturn(ASSET_NAME);
  }

  @Test
  void getResourceDetail_shouldReturnCorrectResourceDetail() {
    Map<String, String> labels =
        Map.of(
            "environment", "production",
            "team", "platform");

    when(function.getName())
        .thenReturn("projects/test-project/locations/europe-west1/functions/test-function");
    when(function.getLabelsMap()).thenReturn(labels);
    when(client.getFunction(any(GetFunctionRequest.class))).thenReturn(function);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createFunctionsClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertNotNull(result);

      assertEquals(
          "projects/test-project/locations/europe-west1/functions/test-function",
          result.getResourceId());
      assertEquals("test-function", result.getName());
      assertEquals("function_name", result.getResourceType());
      assertEquals("cloud_function", result.getServiceCategory());
      assertEquals("europe-west1", result.getRegion());
      assertEquals(labels, result.getTags());
    }

    verify(client).getFunction(any(GetFunctionRequest.class));
  }

  @Test
  void getResourceDetail_shouldUseProjectLocationAndFunctionNameFromAssetName() {
    when(resource.getName())
        .thenReturn(
            "//cloudfunctions.googleapis.com/projects/my-project/locations/us-central1/functions/my-function");

    when(function.getName())
        .thenReturn("projects/my-project/locations/us-central1/functions/my-function");
    when(function.getLabelsMap()).thenReturn(Map.of());
    when(client.getFunction(any(GetFunctionRequest.class))).thenReturn(function);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createFunctionsClient(credentials)).thenReturn(client);

      service.getResourceDetail(resource, credentials);

      verify(client)
          .getFunction(
              argThat(
                  (GetFunctionRequest request) ->
                      request
                          .getName()
                          .equals(
                              "projects/my-project/locations/us-central1/functions/my-function")));
    }
  }

  @Test
  void getResourceDetail_shouldPreserveFunctionLabels() {
    Map<String, String> labels =
        Map.of(
            "app", "cloudsherpa",
            "environment", "test",
            "owner", "platform");

    when(function.getName())
        .thenReturn("projects/test-project/locations/europe-west1/functions/test-function");
    when(function.getLabelsMap()).thenReturn(labels);
    when(client.getFunction(any(GetFunctionRequest.class))).thenReturn(function);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createFunctionsClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals(labels, result.getTags());
    }
  }

  @Test
  void getResourceDetail_shouldUseFunctionNameFromIdentifier() {
    when(function.getName())
        .thenReturn("projects/test-project/locations/europe-west1/functions/test-function");
    when(function.getLabelsMap()).thenReturn(Map.of());
    when(client.getFunction(any(GetFunctionRequest.class))).thenReturn(function);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createFunctionsClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals("test-function", result.getName());
    }
  }

  @Test
  void getResourceDetail_shouldUseFunctionResourceNameAsResourceId() {
    String functionResourceName =
        "projects/test-project/locations/europe-west1/functions/test-function";

    when(function.getName()).thenReturn(functionResourceName);
    when(function.getLabelsMap()).thenReturn(Map.of());
    when(client.getFunction(any(GetFunctionRequest.class))).thenReturn(function);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createFunctionsClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertEquals(functionResourceName, result.getResourceId());
    }
  }

  @Test
  void getResourceDetail_shouldCloseClientAfterSuccessfulLookup() {
    when(function.getName())
        .thenReturn("projects/test-project/locations/europe-west1/functions/test-function");
    when(function.getLabelsMap()).thenReturn(Map.of());
    when(client.getFunction(any(GetFunctionRequest.class))).thenReturn(function);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createFunctionsClient(credentials)).thenReturn(client);

      service.getResourceDetail(resource, credentials);
    }

    verify(client).close();
  }

  @Test
  void getResourceDetail_shouldWrapClientException() {
    RuntimeException cause = new RuntimeException("GCP API failure");

    when(client.getFunction(any(GetFunctionRequest.class))).thenThrow(cause);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createFunctionsClient(credentials)).thenReturn(client);

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
      factory.when(() -> GcpClientFactory.createFunctionsClient(credentials)).thenThrow(cause);

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
      factory.when(() -> GcpClientFactory.createFunctionsClient(credentials)).thenThrow(cause);

      assertThrows(
          IllegalStateException.class, () -> service.getResourceDetail(resource, credentials));
    }

    verifyNoInteractions(client);
    verifyNoInteractions(function);
  }

  @Test
  void getResourceDetail_shouldPropagateEmptyLabels() {
    when(function.getName())
        .thenReturn("projects/test-project/locations/europe-west1/functions/test-function");
    when(function.getLabelsMap()).thenReturn(Map.of());
    when(client.getFunction(any(GetFunctionRequest.class))).thenReturn(function);

    try (MockedStatic<GcpClientFactory> factory = mockStatic(GcpClientFactory.class)) {
      factory.when(() -> GcpClientFactory.createFunctionsClient(credentials)).thenReturn(client);

      ResourceDetail result = service.getResourceDetail(resource, credentials);

      assertNotNull(result);
      assertTrue(result.getTags().isEmpty());
    }
  }
}
