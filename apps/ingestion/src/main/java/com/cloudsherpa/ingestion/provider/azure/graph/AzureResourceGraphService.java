package com.cloudsherpa.ingestion.provider.azure.graph;

import com.azure.resourcemanager.resourcegraph.ResourceGraphManager;
import com.azure.resourcemanager.resourcegraph.models.QueryRequest;
import com.azure.resourcemanager.resourcegraph.models.QueryRequestOptions;
import com.azure.resourcemanager.resourcegraph.models.QueryResponse;
import com.azure.resourcemanager.resourcegraph.models.ResultFormat;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.provider.azure.factory.AzureClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AzureResourceGraphService {

  private static final int PAGE_SIZE = 1000;

  private static final String RESOURCE_QUERY =
      """
      Resources
      | project id, name, type, location, tags
      """;

  private final ObjectMapper objectMapper;

  public AzureResourceGraphService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public List<JsonNode> searchResources(CloudCredentials credentials) {
    ResourceGraphManager manager = AzureClientFactory.createResourceGraphManager(credentials);

    List<JsonNode> resources = new ArrayList<>();
    String skipToken = null;

    do {
      QueryRequestOptions options =
          new QueryRequestOptions().withResultFormat(ResultFormat.OBJECT_ARRAY).withTop(PAGE_SIZE);

      if (skipToken != null) {
        options.withSkipToken(skipToken);
      }

      QueryRequest request =
          new QueryRequest()
              .withSubscriptions(List.of(credentials.getSubscriptionId()))
              .withQuery(RESOURCE_QUERY)
              .withOptions(options);

      QueryResponse response = manager.resourceProviders().resources(request);

      resources.addAll(toJsonNodes(response.data()));
      skipToken = response.skipToken();

    } while (skipToken != null && !skipToken.isBlank());

    return resources;
  }

  private List<JsonNode> toJsonNodes(Object data) {
    return objectMapper.convertValue(
        data, objectMapper.getTypeFactory().constructCollectionType(List.class, JsonNode.class));
  }
}
