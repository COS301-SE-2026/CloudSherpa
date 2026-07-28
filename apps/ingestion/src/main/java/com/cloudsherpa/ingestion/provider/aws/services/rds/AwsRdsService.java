package com.cloudsherpa.ingestion.provider.aws.services.rds;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.model.RegionalDbInstance;
import com.cloudsherpa.ingestion.provider.util.DiscoveryExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.Tag;

@Service
public class AwsRdsService implements RdsService {

  private final Logger logger = Logger.getLogger(getClass().getName());

  private final DiscoveryExecutor discoveryExecutor;

  public AwsRdsService(DiscoveryExecutor discoveryExecutor) {
    this.discoveryExecutor = discoveryExecutor;
  }

  @Override
  public List<RegionalDbInstance> getAllRdsInstances(CloudCredentials credentials) {

    return discoveryExecutor.execute(
        Region.regions(), region -> discoverInstances(region, credentials));
  }

  private List<RegionalDbInstance> discoverInstances(Region region, CloudCredentials credentials) {

    List<RegionalDbInstance> resources = new ArrayList<>();

    try (RdsClient rds =
        RdsClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      List<DBInstance> instances =
          rds.describeDBInstancesPaginator().dbInstances().stream().toList();

      if (!instances.isEmpty()) {
        resources.add(new RegionalDbInstance(instances, region));
      }

    } catch (Exception e) {
      logger.info("Skipping RDS discovery for region " + region.id() + ": " + e.getMessage());
    }

    return resources;
  }

  @Override
  public List<ResourceDetail> getAllRdsInstancesWithTags(CloudCredentials credentials) {

    return discoveryExecutor.execute(
        Region.regions(), region -> discoverInstancesWithTags(region, credentials));
  }

  private List<ResourceDetail> discoverInstancesWithTags(
      Region region, CloudCredentials credentials) {

    List<ResourceDetail> resources = new ArrayList<>();

    try (RdsClient rds =
        RdsClient.builder()
            .region(region)
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      List<DBInstance> instances =
          rds.describeDBInstancesPaginator().dbInstances().stream().toList();

      for (DBInstance db : instances) {

        try {

          Map<String, String> tags =
              rds.listTagsForResource(r -> r.resourceName(db.dbInstanceArn())).tagList().stream()
                  .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> b));

          String name =
              ResourceDetail.resolveName(
                  db.dbInstanceIdentifier(), db.dbInstanceIdentifier(), tags);

          resources.add(
              new ResourceDetail(
                  db.dbInstanceIdentifier(),
                  name,
                  "DBInstanceIdentifier",
                  "AWS/RDS",
                  region.id(),
                  tags));

        } catch (Exception e) {
          logger.info(
              "Skipping RDS instance "
                  + db.dbInstanceIdentifier()
                  + " in region "
                  + region.id()
                  + ": "
                  + e.getMessage());
        }
      }

    } catch (Exception e) {
      logger.info("Skipping RDS discovery for region " + region.id() + ": " + e.getMessage());
    }

    return resources;
  }
}
