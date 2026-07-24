package com.cloudsherpa.ingestion.provider.aws.services.ec2;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Tag;

public class AwsEc2Service implements Ec2Service {
  @Override
  public List<RegionalInstance> getAllEc2Instances(CloudCredentials credentials) {
    List<RegionalInstance> resources = new ArrayList<>();
    for (Region region : Region.regions()) {
      try (Ec2Client ec2 = Ec2Client.builder()
          .region(region)
          .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
          .build()) {

        DescribeInstancesResponse response = ec2.describeInstances();

        for (Reservation reservation : response.reservations()) {
          for (Instance instance : reservation.instances()) {

            resources.add(new RegionalInstance(instance, region));
          }
        }
      } catch (Exception e) {
        System.out.println(
            "Skipping EC2 discovery for region " + region.id() + ": " + e.getMessage());
      }
    }

    return resources;
  }

  @Override
  public Map<String, String> getTagsForInstance(Instance instance) {
    return instance.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));
  }

  @Override
  public List<ResourceDetail> getAllEc2InstancesWithTags(CloudCredentials credentials) {
    List<RegionalInstance> instances = getAllEc2Instances(credentials);
    List<ResourceDetail> resources = new ArrayList<>();

    for (RegionalInstance instance : instances) {
      Map<String, String> tags = getTagsForInstance(instance.instance());
      String instanceName = ResourceDetail.resolveName(instance.instance().instanceId(), null, tags);
      resources.add(
          new ResourceDetail(
              instance.instance().instanceId(),
              instanceName,
              "InstanceId",
              "AWS/EC2",
              instance.region().id(),
              tags));
    }

    return resources;
  }
}
