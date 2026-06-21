package com.cloudsherpa.ingestion.provider.aws.services.RdsService;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.rds.model.Tag;

public class AwsRdsService implements RdsService {
  @Override
  public List<DBInstance> getAllRdsInstances(CloudCredentials credentials) {
    List<DBInstance> instances = new ArrayList<>();

    try (RdsClient rds =
        RdsClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      DescribeDbInstancesResponse response = rds.describeDBInstances();
      instances = response.dbInstances();
    }

    return instances;
  }

  @Override
  public List<ResourceDetail> getAllRdsInstancesWithTags(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();

    try (RdsClient rds =
        RdsClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      for (DBInstance db : getAllRdsInstances(credentials)) {

        Map<String, String> tags =
            rds.listTagsForResource(r -> r.resourceName(db.dbInstanceArn())).tagList().stream()
                .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));
        String name =
            ResourceDetail.resolveName(db.dbInstanceIdentifier(), db.dbInstanceIdentifier(), tags);
        resources.add(
            new ResourceDetail(
                db.dbInstanceIdentifier(), name, "DBInstanceIdentifier", "RDS", tags));
      }
    }

    return resources;
  }
}
