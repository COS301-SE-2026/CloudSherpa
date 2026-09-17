package com.cloudsherpa.ingestion.unit.provider.azure.services.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.azure.services.vm.AzureVirtualMachineScanner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AzureVirtualMachineScannerTest {

  private static final String RESOURCE_ID =
      "/subscriptions/sub-id/resourceGroups/test-rg/providers/"
          + "Microsoft.Compute/virtualMachines/test-vm";

  private static final String NAME = "test-vm";
  private static final String REGION = "eastus";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private CloudCredentials credentials;

  private AzureVirtualMachineScanner scanner;

  @BeforeEach
  void setUp() {
    scanner = new AzureVirtualMachineScanner();
  }

  @Test
  void getResourceTypesReturnsVirtualMachineType() {
    assertEquals(
        java.util.List.of("microsoft.compute/virtualmachines"), scanner.getResourceTypes());
  }

  @Test
  void scanReturnsResourceDetailsWithTags() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/test-vm",
              "name": "test-vm",
              "location": "eastus",
              "tags": {
                "environment": "test",
                "owner": "cloudsherpa"
              }
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertEquals(RESOURCE_ID, result.getResourceId());
    assertEquals(NAME, result.getName());
    assertEquals("resource_id", result.getResourceType());
    assertEquals("Microsoft.Compute/virtualMachines", result.getServiceCategory());
    assertEquals(REGION, result.getRegion());
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
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/test-vm",
              "name": "test-vm",
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
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/test-vm",
              "name": "test-vm",
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
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/test-vm",
              "name": "",
              "location": "eastus",
              "tags": {
                "Name": "tag-based-vm-name"
              }
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertEquals("tag-based-vm-name", result.getName());
  }

  @Test
  void scanFallsBackToResourceIdWhenNameAndTagNameAreMissing() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/test-vm",
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
