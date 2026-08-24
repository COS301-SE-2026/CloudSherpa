package com.cloudsherpa.ingestion.provider.aws.services.lambda;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AwsLambdaScanner implements ResourceScanner {
  private final LambdaService lambdaService;
  private final LambdaPermissionsService permissionsService;

  public AwsLambdaScanner(
      LambdaService lambdaService, LambdaPermissionsService permissionsService) {
    this.lambdaService = lambdaService;
    this.permissionsService = permissionsService;
  }

  @Override
  public String getProvider() {
    return "AWS";
  }

  @Override
  public String getServiceName() {
    return "AWS/Lambda";
  }

  @Override
  public List<ResourceDetail> scan(CloudCredentials credentials) {
    return lambdaService.getAllLambdaFunctionsWithTags(credentials);
  }

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }
}
