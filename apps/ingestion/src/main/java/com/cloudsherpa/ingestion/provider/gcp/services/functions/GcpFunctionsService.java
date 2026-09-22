package com.cloudsherpa.ingestion.provider.gcp.services.functions;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.google.cloud.asset.v1.ResourceSearchResult;
import com.google.cloud.functions.v2.Function;
import com.google.cloud.functions.v2.FunctionServiceClient;
import com.google.cloud.functions.v2.GetFunctionRequest;
import org.springframework.stereotype.Service;

@Service
public class GcpFunctionsService implements FunctionsService {

  @Override
  public ResourceDetail getResourceDetail(
      ResourceSearchResult resource, CloudCredentials credentials) {

    FunctionsResourceIdentifier identifier =
        FunctionsResourceIdentifier.fromAssetName(resource.getName());

    String functionName =
        String.format(
            "projects/%s/locations/%s/functions/%s",
            identifier.projectId(), identifier.location(), identifier.functionName());

    try (FunctionServiceClient client = GcpClientFactory.createFunctionsClient(credentials)) {

      Function function =
          client.getFunction(GetFunctionRequest.newBuilder().setName(functionName).build());

      return new ResourceDetail(
          function.getName(),
          identifier.functionName(),
          "function_name",
          "cloud_function",
          identifier.location(),
          function.getLabelsMap());

    } catch (Exception e) {
      throw new IllegalStateException(
          "Unable to describe GCP Cloud Function " + resource.getName(), e);
    }
  }
}
