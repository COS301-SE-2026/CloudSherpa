package com.cloudsherpa.ingestion.unit.provider.azure.services.postgresql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.azure.services.postgresql.AzurePostgreSqlScanner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AzurePostgreSqlScannerTest {

  private static final String RESOURCE_ID =
      "/subscriptions/sub-id/resourceGroups/test-rg/providers/"
          + "Microsoft.DBforPostgreSQL/flexibleServers/test-postgres";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private CloudCredentials credentials;

  private AzurePostgreSqlScanner scanner;

  @BeforeEach
  void setUp() {
    scanner = new AzurePostgreSqlScanner();
  }

  @Test
  void getResourceTypesReturnsFlexibleServerType() {
    assertEquals(
        java.util.List.of("microsoft.dbforpostgresql/flexibleservers"), scanner.getResourceTypes());
  }

  @Test
  void scanReturnsResourceDetailsWithTags() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.DBforPostgreSQL/flexibleServers/test-postgres",
              "name": "test-postgres",
              "location": "eastus",
              "tags": {
                "environment": "test",
                "database": "postgresql"
              }
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertEquals(RESOURCE_ID, result.getResourceId());
    assertEquals("test-postgres", result.getName());
    assertEquals("resource_id", result.getResourceType());
    assertEquals("Microsoft.DBforPostgreSQL/flexibleServers", result.getServiceCategory());
    assertEquals("eastus", result.getRegion());
    assertEquals(
        Map.of(
            "environment", "test",
            "database", "postgresql"),
        result.getTags());

    verifyNoInteractions(credentials);
  }

  @Test
  void scanReturnsEmptyTagsWhenTagsAreMissing() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.DBforPostgreSQL/flexibleServers/test-postgres",
              "name": "test-postgres",
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
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.DBforPostgreSQL/flexibleServers/test-postgres",
              "name": "test-postgres",
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
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.DBforPostgreSQL/flexibleServers/test-postgres",
              "name": "",
              "location": "eastus",
              "tags": {
                "Name": "postgres-from-tag"
              }
            }
            """);

    ResourceDetail result = scanner.scan(resource, credentials);

    assertEquals("postgres-from-tag", result.getName());
  }

  @Test
  void scanFallsBackToResourceIdWhenNameAndTagNameAreMissing() throws Exception {
    JsonNode resource =
        objectMapper.readTree(
            """
            {
              "id": "/subscriptions/sub-id/resourceGroups/test-rg/providers/Microsoft.DBforPostgreSQL/flexibleServers/test-postgres",
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
