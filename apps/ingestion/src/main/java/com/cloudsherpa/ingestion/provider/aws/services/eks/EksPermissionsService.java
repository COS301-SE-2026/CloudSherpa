package com.cloudsherpa.ingestion.provider.aws.services.eks;

import com.cloudsherpa.ingestion.provider.aws.permissions.AwsPermissionsService;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class EksPermissionsService implements AwsPermissionsService {

  @Override
  public Set<String> getPermissionsRequired() {
    return Set.of(
        "eks:ListClusters",
        "eks:DescribeCluster",
        "eks:ListNodegroups",
        "eks:DescribeNodegroup",
        "eks:ListTagsForResource");
  }
}
