package com.cloudsherpa.ingestion.provider.aws.services.redshift;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AwsRedshiftScanner implements ResourceScanner {
  private final RedshiftService redshiftService;
  private final RedshiftPermissionsService permissionsService;

  public AwsRedshiftScanner(
      RedshiftService redshiftService, RedshiftPermissionsService permissionsService) {
    this.redshiftService = redshiftService;
    this.permissionsService = permissionsService;
  }

  @Override
  public String getProvider() {
    return "AWS";
  }

  @Override
  public String getServiceName() {
    return "AWS/Redshift";
  }

  @Override
  public List<ResourceDetail> scan(CloudCredentials credentials) {
    return redshiftService.getAllRedshiftClustersWithTags(credentials);
  }

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }
}
