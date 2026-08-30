package com.cloudsherpa.ingestion.provider.aws.services.elasticache;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AwsElastiCacheScanner implements ResourceScanner {
  private final ElastiCacheService elasticacheService;
  private final ElastiCachePermissionsService permissionsService;

  public AwsElastiCacheScanner(
      ElastiCacheService elasticacheService, ElastiCachePermissionsService permissionsService) {
    this.elasticacheService = elasticacheService;
    this.permissionsService = permissionsService;
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

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }
}
