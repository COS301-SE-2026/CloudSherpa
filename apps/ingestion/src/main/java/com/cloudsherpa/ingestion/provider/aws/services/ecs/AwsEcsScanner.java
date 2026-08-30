package com.cloudsherpa.ingestion.provider.aws.services.ecs;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AwsEcsScanner implements ResourceScanner {
  private final EcsService ecsService;
  private final EcsPermissionsService permissionsService;

  public AwsEcsScanner(EcsService ecsService, EcsPermissionsService permissionsService) {
    this.ecsService = ecsService;
    this.permissionsService = permissionsService;
  }

  @Override
  public String getProvider() {
    return "AWS";
  }

  @Override
  public String getServiceName() {
    return "AWS/ECS";
  }

  @Override
  public List<ResourceDetail> scan(CloudCredentials credentials) {
    return ecsService.getAllEcsClustersWithTags(credentials);
  }

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }
}
