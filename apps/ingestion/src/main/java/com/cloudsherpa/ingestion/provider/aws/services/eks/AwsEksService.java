package com.cloudsherpa.ingestion.provider.aws.services.eks;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import java.util.ArrayList;
import java.util.List;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.Cluster;

public class AwsEksService implements EksService {
  @Override
  public List<String> getAllEksClusterArns(CloudCredentials credentials) {
    try (EksClient eks =
        EksClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      return eks.listClusters().clusters();
    }
  }

  @Override
  public List<ResourceDetail> getAllEksClustersWithTags(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();

    try (EksClient eks =
        EksClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      for (String clusterName : eks.listClusters().clusters()) {

        Cluster cluster = eks.describeCluster(r -> r.name(clusterName)).cluster();
        String name = ResourceDetail.resolveName(clusterName, cluster.name(), cluster.tags());
        resources.add(new ResourceDetail(clusterName, name, "ClusterName", "EKS", cluster.tags()));
      }
    }

    return resources;
  }
}
