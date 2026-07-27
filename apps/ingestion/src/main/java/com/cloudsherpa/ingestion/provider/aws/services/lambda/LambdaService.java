package com.cloudsherpa.ingestion.provider.aws.services.lambda;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import java.util.List;

public interface LambdaService {
  public List<RegionalArn> getAllLambdaFunctionArns(CloudCredentials credentials);

  public List<ResourceDetail> getAllLambdaFunctionsWithTags(CloudCredentials credentials);
}
