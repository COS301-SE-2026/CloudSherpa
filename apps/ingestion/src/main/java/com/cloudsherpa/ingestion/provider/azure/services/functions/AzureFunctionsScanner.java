package com.cloudsherpa.ingestion.provider.azure.services.functions;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.azure.scanner.AzureResourceScanner;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AzureFunctionsScanner implements AzureResourceScanner {

  @Override
  public String getServiceName() {
    return "AZURE/FUNCTIONS";
  }

  @Override
  public List<String> getResourceTypes() {
    return List.of("microsoft.web/sites");
  }

  @Override
  public ResourceDetail scan(JsonNode resource, CloudCredentials credentials) {

    String resourceId = resource.path("id").asText();
    String name = resource.path("name").asText();
    String location = resource.path("location").asText();

    Map<String, String> tags = extractTags(resource.path("tags"));

    return new ResourceDetail(
        resourceId,
        ResourceDetail.resolveName(resourceId, name, tags),
        "resource_id",
        "Microsoft.Web/sites",
        location,
        tags);
  }

  private Map<String, String> extractTags(JsonNode tags) {
    if (!tags.isObject()) {
      return Map.of();
    }

    Map<String, String> result = new HashMap<>();

    tags.properties().forEach(entry -> result.put(entry.getKey(), entry.getValue().asText()));

    return result;
  }

  @Override
  public Set<String> getPermissionsRequired() {
    return Set.of();
  }
}
