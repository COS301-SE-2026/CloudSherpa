package com.cloudsherpa.ingestion.provider.aws.services.redshift;

import com.cloudsherpa.ingestion.provider.permissions.PermissionsService;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RedshiftPermissionsService implements PermissionsService {

  @Override
  public Set<String> getPermissionsRequired() {
    return Set.of(
        "redshift:DescribeClusters",
        "redshift:DescribeClusterSnapshots",
        "redshift:DescribeClusterSubnetGroups",
        "redshift:DescribeClusterParameterGroups",
        "redshift:DescribeTags");
  }
}
