package com.cloudsherpa.ingestion.unit.provider.azure.services.aks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.azure.services.aks.AzureKubernetesScanner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AzureKubernetesScannerTest {

  private static final String RESOURCE_ID =
      "/subscriptions/sub-id/resourceGroups/test-rg/providers/"
          + "Microsoft.ContainerService/managedClusters/test-aks";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private CloudCredentials credentials;

  private AzureKubernetesScanner scanner;

  @BeforeEach
  void setUp() {
    scanner = new AzureKubernetesScanner();
  }

  @Test
  void getServiceNameReturnsAzureAks() {
    assertEquals("AZURE/AKS", scanner.getServiceName());
  }

  @Test
  void getResourceTypesReturnsManagedClusterType() {
    assertEquals(
        java.util.List.of("microsoft.containerservice/managedclusters"),
        scanner.getResourceTypes());
  }

  @Test
  void scanReturnsResourceDetailsWithTags() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.ContainerService/managedClusters/test-aks",
              "name": "test-aks",
              "location": "eastus",
              "tags": {
                "environment": "test",
                "owner": "cloudsherpa"
              }
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertEquals(RESOURCE_ID, result.getResourceId());
    assertEquals("test-aks", result.getName());
    assertEquals("resource_id", result.getResourceType());
    assertEquals("Microsoft.ContainerService/managedClusters", result.getServiceCategory());
    assertEquals("eastus", result.getRegion());
    assertEquals(
        Map.of(
            "environment", "test",
            "owner", "cloudsherpa"),
        result.getTags());

    verifyNoInteractions(credentials);
  }

  @Test
  void scanReturnsEmptyTagsWhenTagsAreMissing() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.ContainerService/managedClusters/test-aks",
              "name": "test-aks",
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
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.ContainerService/managedClusters/test-aks",
              "name": "test-aks",
              "location": "eastus",
              "tags": []
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
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.ContainerService/managedClusters/test-aks",
              "name": "",
              "location": "eastus",
              "tags": {
                "Name": "aks-from-tag"
              }
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertEquals("aks-from-tag", result.getName());
  }

  @Test
  void scanFallsBackToResourceIdWhenNameAndTagNameAreMissing() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.ContainerService/managedClusters/test-aks",
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
