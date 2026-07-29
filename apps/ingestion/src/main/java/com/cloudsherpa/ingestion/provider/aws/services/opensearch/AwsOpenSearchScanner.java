package com.cloudsherpa.ingestion.provider.aws.services.opensearch;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AwsOpenSearchScanner implements ResourceScanner {
  private final OpenSearchService opensearchService;

  public AwsOpenSearchScanner(OpenSearchService opensearchService) {
    this.opensearchService = opensearchService;
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
}
