package com.cloudsherpa.ingestion.provider.aws.services.OpenSearchService;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import java.util.List;
import software.amazon.awssdk.services.opensearch.model.DomainInfo;

public interface OpenSearchService {
  public List<DomainInfo> getAllOpenSearchDomains(CloudCredentials credentials);

  public List<ResourceDetail> getAllOpenSearchDomainsWithTags(CloudCredentials credentials);
}
