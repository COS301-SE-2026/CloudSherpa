package com.cloudsherpa.ingestion.provider.aws.services.eks;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AwsEksScanner implements ResourceScanner {
  private final EksService eksService;

  public AwsEksScanner(EksService eksService) {
    this.eksService = eksService;
  }

  @Override
  public String getProvider() {
    return "AWS";
  }

  @Override
  public String getServiceName() {
    return "ContainerInsights";
  }

  @Override
  public List<ResourceDetail> scan(CloudCredentials credentials) {
    return eksService.getAllEksClustersWithTags(credentials);
  }
}
