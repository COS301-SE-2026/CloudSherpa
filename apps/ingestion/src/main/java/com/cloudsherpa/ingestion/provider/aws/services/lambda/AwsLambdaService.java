package com.cloudsherpa.ingestion.provider.aws.services.lambda;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse;

public class AwsLambdaService implements LambdaService {
  @Override
  public List<RegionalArn> getAllLambdaFunctionArns(CloudCredentials credentials) {
    List<String> functionArns = new ArrayList<>();
    List<RegionalArn> regionalArns = new ArrayList<>();
    for (Region region : Region.regions()) {
      try (LambdaClient lambda =
          LambdaClient.builder()
              .region(region)
              .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
              .build()) {

        ListFunctionsResponse response = lambda.listFunctions();

        for (FunctionConfiguration fn : response.functions()) {
          functionArns.add(fn.functionArn());
        }
        regionalArns.add(new RegionalArn(functionArns, region));
      } catch (Exception e) {
        System.out.println(
            "Skipping Lambda discovery for region " + region.id() + ": " + e.getMessage());
      }
    }
    return regionalArns;
  }

  @Override
  public List<ResourceDetail> getAllLambdaFunctionsWithTags(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();
    for (Region region : Region.regions()) {
      try (LambdaClient lambda =
          LambdaClient.builder()
              .region(region)
              .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
              .build()) {

        ListFunctionsResponse response = lambda.listFunctions();

        for (FunctionConfiguration fn : response.functions()) {
          Map<String, String> tags = lambda.listTags(r -> r.resource(fn.functionArn())).tags();
          String name = ResourceDetail.resolveName(fn.functionName(), fn.functionName(), tags);
          resources.add(
              new ResourceDetail(
                  fn.functionName(), name, "FunctionName", "AWS/Lambda", region.id(), tags));
        }
      } catch (Exception e) {
        System.out.println(
            "Skipping Lambda discovery for region " + region.id() + ": " + e.getMessage());
      }
    }
    return resources;
  }
}
