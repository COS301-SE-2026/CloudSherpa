package com.cloudsherpa.ingestion.provider.aws.services.lambda;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;

@Service
public class AwsLambdaService implements LambdaService {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final DiscoveryExecutor discoveryExecutor;

  public AwsLambdaService(DiscoveryExecutor discoveryExecutor) {
    this.discoveryExecutor = discoveryExecutor;
  }

  @Override
  public List<RegionalArn> getAllLambdaFunctionArns(CloudCredentials credentials) {

    return discoveryExecutor.execute(
        Region.regions(), region -> discoverFunctionArns(region, credentials));
  }

  private List<RegionalArn> discoverFunctionArns(Region region, CloudCredentials credentials) {

    List<RegionalArn> resources = new ArrayList<>();
    List<String> functionArns = new ArrayList<>();

    try (LambdaClient lambda =
        LambdaClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      lambda.listFunctionsPaginator().functions().stream()
          .map(FunctionConfiguration::functionArn)
          .forEach(functionArns::add);

      if (!functionArns.isEmpty()) {
        resources.add(new RegionalArn(functionArns, region));
      }

    } catch (Exception e) {
      logger.info("Skipping Lambda discovery for region " + region.id() + ": " + e.getMessage());
    }

    return resources;
  }

  @Override
  public List<ResourceDetail> getAllLambdaFunctionsWithTags(CloudCredentials credentials) {
    return discoveryExecutor.execute(
        Region.regions(), region -> discoverFunctionsWithTags(region, credentials));
  }

  private List<ResourceDetail> discoverFunctionsWithTags(
      Region region, CloudCredentials credentials) {

    List<ResourceDetail> resources = new ArrayList<>();

    try (LambdaClient lambda =
        LambdaClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      for (FunctionConfiguration function : lambda.listFunctionsPaginator().functions()) {

        try {
          Map<String, String> tags =
              lambda.listTags(r -> r.resource(function.functionArn())).tags();

          String name =
              ResourceDetail.resolveName(function.functionName(), function.functionName(), tags);

          resources.add(
              new ResourceDetail(
                  function.functionName(), name, "FunctionName", "AWS/Lambda", region.id(), tags));

        } catch (Exception e) {
          logger.info(
              "Skipping Lambda function "
                  + function.functionName()
                  + " in region "
                  + region.id()
                  + ": "
                  + e.getMessage());
        }
      }

    } catch (Exception e) {
      logger.info("Skipping Lambda discovery for region " + region.id() + ": " + e.getMessage());
    }
    return resources;
  }
}
