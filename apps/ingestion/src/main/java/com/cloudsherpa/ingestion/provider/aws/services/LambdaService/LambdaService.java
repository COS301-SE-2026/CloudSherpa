package com.cloudsherpa.ingestion.provider.aws.services.LambdaService;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import java.util.List;

public interface LambdaService {
  public List<String> getAllLambdaFunctionArns(CloudCredentials credentials);

  public List<ResourceDetail> getAllLambdaFunctionsWithTags(CloudCredentials credentials);
}
