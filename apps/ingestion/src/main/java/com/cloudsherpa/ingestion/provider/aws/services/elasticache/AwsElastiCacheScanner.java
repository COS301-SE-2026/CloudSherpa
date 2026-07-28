package com.cloudsherpa.ingestion.provider.aws.services.elasticache;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AwsElastiCacheScanner implements ResourceScanner {
  private final ElastiCacheService elasticacheService;

  public AwsElastiCacheScanner(ElastiCacheService elasticacheService) {
    this.elasticacheService = elasticacheService;
  }

  @Override
  public String getProvider() {
    return "AWS";
  }

  @Override
  public String getServiceName() {
    return "AWS/ElastiCache";
  }

  @Override
  public List<ResourceDetail> scan(CloudCredentials credentials) {
    return elasticacheService.getAllElastiCacheClustersWithTags(credentials);
  }
}
