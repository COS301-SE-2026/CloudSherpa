package com.cloudsherpa.service.resourcediscovery.client;

import com.cloudsherpa.service.resourcediscovery.dto.ResourceDetailDto;
import com.cloudsherpa.service.resourcediscovery.dto.ResourceDiscoveryDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IngestionResourceClient {

  private final RestClient restClient;

  public IngestionResourceClient(@Value("${INGESTION_BASE_URL}") String ingestionBaseUrl) {

    this.restClient = RestClient.builder().baseUrl(ingestionBaseUrl).build();
  }

  public List<String> getServices(String provider) {

    return restClient
        .post()
        .uri("/api/cloud-resources/services")
        .body(provider)
        .retrieve()
        .body(new ParameterizedTypeReference<List<String>>() {});
  }

  public List<ResourceDetailDto> getResources(String provider, ResourceDiscoveryDto request) {

    return restClient
        .post()
        .uri("/api/cloud-resources/resources/{provider}", provider)
        .body(request)
        .retrieve()
        .body(new ParameterizedTypeReference<List<ResourceDetailDto>>() {});
  }

  public String generateAwsPermissions(List<String> services) {

    return restClient
        .post()
        .uri("/api/cloud-resources/aws/permissions")
        .body(services)
        .retrieve()
        .body(String.class);
  }
}
