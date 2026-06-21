package com.cloudsherpa.ingestion.provider.aws.services.lambda;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse;

public class AwsLambdaService implements LambdaService {
  @Override
  public List<String> getAllLambdaFunctionArns(CloudCredentials credentials) {
    List<String> functionArns = new ArrayList<>();

    try (LambdaClient lambda =
        LambdaClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      ListFunctionsResponse response = lambda.listFunctions();

      for (FunctionConfiguration fn : response.functions()) {
        functionArns.add(fn.functionArn());
      }
    }
    return functionArns;
  }

  @Override
  public List<ResourceDetail> getAllLambdaFunctionsWithTags(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();

    try (LambdaClient lambda =
        LambdaClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      ListFunctionsResponse response = lambda.listFunctions();

      for (FunctionConfiguration fn : response.functions()) {
        Map<String, String> tags = lambda.listTags(r -> r.resource(fn.functionArn())).tags();
        String name = ResourceDetail.resolveName(fn.functionName(), fn.functionName(), tags);
        resources.add(new ResourceDetail(fn.functionName(), name, "FunctionName", "LAMBDA", tags));
      }
    }
    return resources;
  }
}
