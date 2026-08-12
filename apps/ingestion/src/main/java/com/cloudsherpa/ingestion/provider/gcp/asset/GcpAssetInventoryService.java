package com.cloudsherpa.ingestion.provider.gcp.asset;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.asset.v1.Asset;
import com.google.cloud.asset.v1.AssetServiceClient;
import com.google.cloud.asset.v1.AssetServiceSettings;
import com.google.cloud.asset.v1.ContentType;
import com.google.cloud.asset.v1.ListAssetsRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GcpAssetInventoryService {

  public List<Asset> listAssets(CloudCredentials credentials) {

    try (AssetServiceClient client = createClient(credentials)) {

      ListAssetsRequest request =
          ListAssetsRequest.newBuilder()
              .setParent("projects/" + credentials.getProjectId())
              .setContentType(ContentType.RESOURCE)
              .build();

      Iterable<Asset> assets = client.listAssets(request).iterateAll();

      List<Asset> assetsList = new ArrayList<>();

      for (Asset asset : assets) {
        assetsList.add(asset);
      }
      return assetsList;

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
