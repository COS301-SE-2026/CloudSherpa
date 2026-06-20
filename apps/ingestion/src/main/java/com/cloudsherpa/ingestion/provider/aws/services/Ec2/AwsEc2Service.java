package com.cloudsherpa.ingestion.provider.aws.services.Ec2;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.AwsClientFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Tag;

public class AwsEc2Service implements Ec2Service {
  public List<Instance> getAllEc2Instances(CloudCredentials credentials) {
    List<Instance> resources = new ArrayList<>();

    try (Ec2Client ec2 =
        Ec2Client.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      DescribeInstancesResponse response = ec2.describeInstances();

      for (Reservation reservation : response.reservations()) {
        for (Instance instance : reservation.instances()) {

          resources.add(instance);
        }
      }
    }

    return resources;
  }

  public Map<String, String> getTagsForInstance(Instance instance) {
    return instance.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));
  }

  public List<ResourceDetail> getAllEc2InstancesWithTags(CloudCredentials credentials) {
    List<Instance> instances = getAllEc2Instances(credentials);
    List<ResourceDetail> resources = new ArrayList<>();

    for (Instance instance : instances) {
      Map<String, String> tags = getTagsForInstance(instance);
      String instanceName = ResourceDetail.resolveName(instance.instanceId(), null, tags);
      resources.add(
          new ResourceDetail(instance.instanceId(), instanceName, "InstanceId", "EC2", tags));
    }

    return resources;
  }
}
