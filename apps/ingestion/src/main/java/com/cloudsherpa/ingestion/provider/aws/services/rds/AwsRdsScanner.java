package com.cloudsherpa.ingestion.provider.aws.services.rds;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AwsRdsScanner implements ResourceScanner {
  private final RdsService rdsService;

  @Autowired
  public AwsRdsScanner() {
    this.rdsService = new AwsRdsService();
  }

  public AwsRdsScanner(RdsService rdsService) {
    this.rdsService = rdsService;
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
}
