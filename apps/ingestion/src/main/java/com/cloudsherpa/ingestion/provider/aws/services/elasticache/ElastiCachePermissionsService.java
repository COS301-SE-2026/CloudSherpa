package com.cloudsherpa.ingestion.provider.aws.services.elasticache;

import com.cloudsherpa.ingestion.provider.permissions.PermissionsService;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ElastiCachePermissionsService implements PermissionsService {

  @Override
  public Set<String> getPermissionsRequired() {
    return Set.of(
        "elasticache:DescribeCacheClusters",
        "elasticache:DescribeReplicationGroups",
        "elasticache:DescribeSnapshots",
        "elasticache:ListTagsForResource");
  }
}
