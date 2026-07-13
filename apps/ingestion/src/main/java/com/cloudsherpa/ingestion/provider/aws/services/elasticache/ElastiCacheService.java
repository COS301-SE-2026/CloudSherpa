package com.cloudsherpa.ingestion.provider.aws.services.elasticache;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import java.util.List;

public interface ElastiCacheService {
  public List<String> getAllElastiCacheClusterArns(CloudCredentials credentials);

  public List<ResourceDetail> getAllElastiCacheClustersWithTags(CloudCredentials credentials);
}
