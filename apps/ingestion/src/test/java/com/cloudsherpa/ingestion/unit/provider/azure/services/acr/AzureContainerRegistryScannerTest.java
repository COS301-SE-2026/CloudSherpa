package com.cloudsherpa.ingestion.unit.provider.azure.services.acr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.azure.services.acr.AzureContainerRegistryScanner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AzureContainerRegistryScannerTest {

  private static final String RESOURCE_ID =
      "/subscriptions/sub-id/resourceGroups/test-rg/providers/"
          + "Microsoft.ContainerRegistry/registries/test-registry";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private CloudCredentials credentials;

  private AzureContainerRegistryScanner scanner;

  @BeforeEach
  void setUp() {
    scanner = new AzureContainerRegistryScanner();
  }

  @Test
  void getResourceTypesReturnsRegistryType() {
    assertEquals(
        java.util.List.of("microsoft.containerregistry/registries"), scanner.getResourceTypes());
  }

  @Test
  void scanReturnsResourceDetailsWithTags() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.ContainerRegistry/registries/test-registry",
              "name": "test-registry",
              "location": "eastus",
              "tags": {
                "environment": "test",
                "team": "platform"
              }
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertEquals(RESOURCE_ID, result.getResourceId());
    assertEquals("test-registry", result.getName());
    assertEquals("resource_id", result.getResourceType());
    assertEquals("Microsoft.ContainerRegistry/registries", result.getServiceCategory());
    assertEquals("eastus", result.getRegion());
    assertEquals(
        Map.of(
            "environment", "test",
            "team", "platform"),
        result.getTags());

    verifyNoInteractions(credentials);
  }

  @Test
  void scanReturnsEmptyTagsWhenTagsAreMissing() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.ContainerRegistry/registries/test-registry",
              "name": "test-registry",
              "location": "eastus"
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertTrue(result.getTags().isEmpty());
  }

  @Test
  void scanReturnsEmptyTagsWhenTagsAreNotAnObject() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.ContainerRegistry/registries/test-registry",
              "name": "test-registry",
              "location": "eastus",
              "tags": "invalid"
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertTrue(result.getTags().isEmpty());
  }

  @Test
  void scanUsesTagNameWhenResourceNameIsBlank() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.ContainerRegistry/registries/test-registry",
              "name": "",
              "location": "eastus",
              "tags": {
                "Name": "registry-from-tag"
              }
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertEquals("registry-from-tag", result.getName());
  }

  @Test
  void scanFallsBackToResourceIdWhenNameAndTagNameAreMissing() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.ContainerRegistry/registries/test-registry",
              "name": "",
              "location": "eastus",
              "tags": {
                "environment": "test"
              }
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertEquals(RESOURCE_ID, result.getName());
  }
}
