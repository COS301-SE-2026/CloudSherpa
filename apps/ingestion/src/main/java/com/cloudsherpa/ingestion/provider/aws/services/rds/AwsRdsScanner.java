package com.cloudsherpa.ingestion.provider.aws.services.rds;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AwsRdsScanner implements ResourceScanner {
  private final RdsService rdsService;
  private final RdsPermissionsService permissionsService;

  public AwsRdsScanner(RdsService rdsService, RdsPermissionsService permissionsService) {
    this.rdsService = rdsService;
    this.permissionsService = permissionsService;
  }

  @Override
  public String getProvider() {
    return "AWS";
  }

  @Override
  public String getServiceName() {
    return "AWS/RDS";
  }

  @Override
  public List<ResourceDetail> scan(CloudCredentials credentials) {
    return rdsService.getAllRdsInstancesWithTags(credentials);
  }

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }
}
