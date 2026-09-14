package com.cloudsherpa.ingestion.provider.gcp.services.gar;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.google.cloud.asset.v1.ResourceSearchResult;
import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient;
import com.google.devtools.artifactregistry.v1.GetRepositoryRequest;
import com.google.devtools.artifactregistry.v1.Repository;
import org.springframework.stereotype.Service;

@Service
public class GcpGarService implements GarService {

  @Override
  public ResourceDetail getResourceDetail(
      ResourceSearchResult resource, CloudCredentials credentials) {

    GarResourceIdentifier identifier = GarResourceIdentifier.fromAssetName(resource.getName());

    String repositoryName = String.format(
        "projects/%s/locations/%s/repositories/%s",
        identifier.projectId(), identifier.location(), identifier.repositoryName());

    try (ArtifactRegistryClient client = GcpClientFactory.createArtifactRegistryClient(credentials)) {

      Repository repository = client.getRepository(GetRepositoryRequest.newBuilder().setName(repositoryName).build());

      return new ResourceDetail(
          identifier.repositoryName(),
          identifier.repositoryName(),
          "repository_id",
          "artifactregistry.googleapis.com/Repository",
          identifier.location(),
          repository.getLabelsMap());

    } catch (Exception e) {
      throw new IllegalStateException(
          "Unable to describe GCP Artifact Registry repository " + resource.getName(), e);
    }
  }
}
