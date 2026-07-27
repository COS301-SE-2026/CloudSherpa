package com.cloudsherpa.ingestion.provider.aws.services.ecs;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AwsEcsScanner implements ResourceScanner {
  private final EcsService ecsService;

  @Autowired
  public AwsEcsScanner() {
    this.ecsService = new AwsEcsService();
  }

  public AwsEcsScanner(EcsService ecsService) {
    this.ecsService = ecsService;
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
}
