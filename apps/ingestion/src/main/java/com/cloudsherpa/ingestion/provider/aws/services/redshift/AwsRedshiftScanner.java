package com.cloudsherpa.ingestion.provider.aws.services.redshift;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AwsRedshiftScanner implements ResourceScanner {
  private final RedshiftService redshiftService;

  public AwsRedshiftScanner() {
    this.redshiftService = new AwsRedshiftService();
  }

  public AwsRedshiftScanner(RedshiftService redshiftService) {
    this.redshiftService = redshiftService;
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
}
