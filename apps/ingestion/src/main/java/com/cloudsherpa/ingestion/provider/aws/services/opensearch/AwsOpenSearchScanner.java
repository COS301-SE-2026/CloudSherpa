package com.cloudsherpa.ingestion.provider.aws.services.opensearch;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AwsOpenSearchScanner implements ResourceScanner {
  private final OpenSearchService opensearchService;
  private final OpenSearchPermissionsService permissionsService;

  public AwsOpenSearchScanner(
      OpenSearchService opensearchService, OpenSearchPermissionsService permissionsService) {
    this.opensearchService = opensearchService;
    this.permissionsService = permissionsService;
  }

  @Override
  public String getProvider() {
    return "AWS";
  }

  @Override
  public String getServiceName() {
    return "AWS/ES";
  }

  @Override
  public List<ResourceDetail> scan(CloudCredentials credentials) {
    return opensearchService.getAllOpenSearchDomainsWithTags(credentials);
  }

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }
}
