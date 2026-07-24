package com.cloudsherpa.ingestion.provider.aws.services.lambda;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;

@Component
public class AwsLambdaScanner implements ResourceScanner {
  private final LambdaService lambdaService;

  public AwsLambdaScanner() {
    this.lambdaService = new AwsLambdaService();
  }

  public AwsLambdaScanner(LambdaService lambdaService) {
    this.lambdaService = lambdaService;
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
}
