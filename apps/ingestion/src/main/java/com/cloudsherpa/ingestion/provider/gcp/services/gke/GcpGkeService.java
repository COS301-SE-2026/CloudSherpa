package com.cloudsherpa.ingestion.provider.gcp.services.gke;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.google.cloud.asset.v1.ResourceSearchResult;
import com.google.container.v1.Cluster;
import com.google.container.v1.GetClusterRequest;
import org.springframework.stereotype.Service;

@Service
public class GcpGkeService implements GkeService {

  @Override
  public ResourceDetail getResourceDetail(
      ResourceSearchResult resource, CloudCredentials credentials) {

    GkeResourceIdentifier identifier = GkeResourceIdentifier.fromAssetName(resource.getName());

    try (com.google.cloud.container.v1.ClusterManagerClient client =
        GcpClientFactory.createClusterManagerClient(credentials)) {

      String clusterResourceName =
          String.format(
              "projects/%s/locations/%s/clusters/%s",
              identifier.projectId(), identifier.location(), identifier.clusterName());

      Cluster cluster =
          client.getCluster(GetClusterRequest.newBuilder().setName(clusterResourceName).build());

      return new ResourceDetail(
          cluster.getId(),
          cluster.getName(),
          "cluster_id",
          "gke_cluster",
          identifier.location(),
          cluster.getResourceLabelsMap());

    } catch (Exception e) {
      throw new IllegalStateException(
          "Unable to describe GCP GKE cluster " + resource.getName(), e);
    }
  }
}
