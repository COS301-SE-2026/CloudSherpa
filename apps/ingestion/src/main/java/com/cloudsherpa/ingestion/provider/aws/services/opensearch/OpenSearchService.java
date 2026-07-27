package com.cloudsherpa.ingestion.provider.aws.services.opensearch;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalDomain;
import java.util.List;

public interface OpenSearchService {
  public List<RegionalDomain> getAllOpenSearchDomains(CloudCredentials credentials);

  public List<ResourceDetail> getAllOpenSearchDomainsWithTags(CloudCredentials credentials);
}
