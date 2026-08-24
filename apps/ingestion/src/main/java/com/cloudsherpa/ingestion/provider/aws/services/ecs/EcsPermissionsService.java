package com.cloudsherpa.ingestion.provider.aws.services.ecs;

import com.cloudsherpa.ingestion.provider.aws.permissions.AwsPermissionsService;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class EcsPermissionsService implements AwsPermissionsService {

  @Override
  public Set<String> getPermissionsRequired() {
    return Set.of(
        "ecs:ListClusters",
        "ecs:DescribeClusters",
        "ecs:ListServices",
        "ecs:DescribeServices",
        "ecs:ListTasks",
        "ecs:DescribeTasks",
        "ecs:ListTagsForResource");
  }
}
