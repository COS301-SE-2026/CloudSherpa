package com.cloudsherpa.ingestion.provider.gcp.asset;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.asset.v1.AssetServiceClient;
import com.google.cloud.asset.v1.AssetServiceSettings;
import com.google.cloud.asset.v1.ResourceSearchResult;
import com.google.cloud.asset.v1.SearchAllResourcesRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GcpAssetInventoryService {

  public List<ResourceSearchResult> searchResources(
      CloudCredentials credentials, List<String> assetTypes) {

    try (AssetServiceClient client = createClient(credentials)) {

      SearchAllResourcesRequest request =
          SearchAllResourcesRequest.newBuilder()
              .setScope("projects/" + credentials.getProjectId())
              .addAllAssetTypes(assetTypes)
              .build();

      Iterable<ResourceSearchResult> resources = client.searchAllResources(request).iterateAll();

      List<ResourceSearchResult> resourcesList = new ArrayList<>();

      for (ResourceSearchResult resource : resources) {
        resourcesList.add(resource);
      }

      return resourcesList;

    } catch (IOException e) {
      throw new IllegalStateException("Unable to query GCP Cloud Asset Inventory", e);
    }
  }

  private AssetServiceClient createClient(CloudCredentials credentials) throws IOException {
    GoogleCredentials googleCredentials = GcpClientFactory.credentials(credentials);
    AssetServiceSettings settings =
        AssetServiceSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
            .build();

    return AssetServiceClient.create(settings);
  }
}
