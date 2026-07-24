package com.cloudsherpa.ingestion.provider.aws.services.eks;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalArn;
import java.util.ArrayList;
import java.util.List;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.Cluster;

public class AwsEksService implements EksService {
  @Override
  public List<RegionalArn> getAllEksClusterArns(CloudCredentials credentials) {
    List<RegionalArn> regionalArns = new ArrayList<>();
    for (Region region : Region.regions()) {
      try (EksClient eks = EksClient.builder()
          .region(region)
          .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
          .build()) {

        regionalArns.add(new RegionalArn(eks.listClusters().clusters(), region));
      } catch (Exception e) {
        System.out.println(
            "Skipping EKS discovery for region " + region.id() + ": " + e.getMessage());
      }
    }
    return regionalArns;
  }

  @Override
  public List<ResourceDetail> getAllEksClustersWithTags(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();
    for (Region region : Region.regions()) {
      try (EksClient eks = EksClient.builder()
          .region(region)
          .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
          .build()) {

        for (String clusterName : eks.listClusters().clusters()) {

          Cluster cluster = eks.describeCluster(r -> r.name(clusterName)).cluster();
          String name = ResourceDetail.resolveName(clusterName, cluster.name(), cluster.tags());
          resources.add(
              new ResourceDetail(
                  clusterName, name, "ClusterName", "ContainerInsights", region.id(), cluster.tags()));
        }
      } catch (Exception e) {
        System.out.println(
            "Skipping EKS discovery for region " + region.id() + ": " + e.getMessage());
      }
    }

    return resources;
  }
}
