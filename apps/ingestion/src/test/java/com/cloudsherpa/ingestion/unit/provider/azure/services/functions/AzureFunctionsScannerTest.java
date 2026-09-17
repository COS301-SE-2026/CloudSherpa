package com.cloudsherpa.ingestion.unit.provider.azure.services.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.azure.services.functions.AzureFunctionsScanner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AzureFunctionsScannerTest {

  private static final String RESOURCE_ID =
      "/subscriptions/sub-id/resourceGroups/test-rg/providers/"
          + "Microsoft.Web/sites/test-function";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private CloudCredentials credentials;

  private AzureFunctionsScanner scanner;

  @BeforeEach
  void setUp() {
    scanner = new AzureFunctionsScanner();
  }

  @Test
  void getResourceTypesReturnsWebSitesType() {
    assertEquals(java.util.List.of("microsoft.web/sites"), scanner.getResourceTypes());
  }

  @ParameterizedTest
  @ValueSource(strings = {"functionapp,linux", "FUNCTIONAPP,linux", "linux,app,functionapp"})
  void scanAcceptsFunctionAppKinds(String kind) throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-function",
              "name": "test-function",
              "location": "eastus",
              "kind": "%s"
            }
            """
                .formatted(kind));

    ResourceDetail result = scanner.scan(resource, credentials);

    assertEquals("test-function", result.getName());
  }

  @ParameterizedTest
  @ValueSource(strings = {"app", "", "   "})
  void scanRejectsInvalidFunctionAppKinds(String kind) throws Exception {
    String kindField = kind.isEmpty() ? "" : ",\n    \"kind\": \"" + kind + "\"";

    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
              "name": "test-app",
              "location": "eastus"%s
            }
            """
                .formatted(kindField));

    ResourceDetail result = scanner.scan(resource, credentials);

    assertNull(result);
  }

  @Test
  void scanReturnsEmptyTagsWhenTagsAreMissing() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-function",
              "name": "test-function",
              "location": "eastus",
              "kind": "functionapp,linux"
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
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-function",
              "name": "test-function",
              "location": "eastus",
              "kind": "functionapp,linux",
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
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-function",
              "name": "",
              "location": "eastus",
              "kind": "functionapp,linux",
              "tags": {
                "Name": "function-from-tag"
              }
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertEquals("function-from-tag", result.getName());
  }

  @Test
  void scanFallsBackToResourceIdWhenNameAndTagNameAreMissing() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-function",
              "name": "",
              "location": "eastus",
              "kind": "functionapp,linux",
              "tags": {
                "environment": "test"
              }
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertEquals(RESOURCE_ID, result.getName());
  }
}
