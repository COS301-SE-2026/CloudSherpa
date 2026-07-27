package com.cloudsherpa.ingestion.provider.aws.services.rds;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalDbInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.rds.model.Tag;

public class AwsRdsService implements RdsService {
  Logger logger = Logger.getLogger(getClass().getName());

  @Override
  public List<RegionalDbInstance> getAllRdsInstances(CloudCredentials credentials) {
    List<DBInstance> instances = new ArrayList<>();
    List<RegionalDbInstance> regionalInstances = new ArrayList<>();
    for (Region region : Region.regions()) {
      try (RdsClient rds =
          RdsClient.builder()
              .region(region)
              .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
              .build()) {

        DescribeDbInstancesResponse response = rds.describeDBInstances();
        instances = response.dbInstances();
        regionalInstances.add(new RegionalDbInstance(instances, region));
      } catch (Exception e) {
        logger.info("Skipping RDS discovery for region " + region.id() + ": " + e.getMessage());
      }
    }
    return regionalInstances;
  }

  @Override
  public List<ResourceDetail> getAllRdsInstancesWithTags(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();

    for (RegionalDbInstance db : getAllRdsInstances(credentials)) {
      try (RdsClient rds =
          RdsClient.builder()
              .region(db.region())
              .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
              .build()) {
        for (DBInstance dbInfo : db.domains()) {
          Map<String, String> tags =
              rds
                  .listTagsForResource(r -> r.resourceName(dbInfo.dbInstanceArn()))
                  .tagList()
                  .stream()
                  .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));
          String name =
              ResourceDetail.resolveName(
                  dbInfo.dbInstanceIdentifier(), dbInfo.dbInstanceIdentifier(), tags);
          resources.add(
              new ResourceDetail(
                  dbInfo.dbInstanceIdentifier(),
                  name,
                  "DBInstanceIdentifier",
                  "AWS/RDS",
                  db.region().id(),
                  tags));
        }
      } catch (Exception e) {
        // Regional logging messages handled by child function
      }
    }

    return resources;
  }
}
