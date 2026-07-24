package com.cloudsherpa.ingestion.provider.aws.services.ec2;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;

@Component
public class AwsEc2Scanner implements ResourceScanner {
  private final Ec2Service ec2Service;

  public AwsEc2Scanner() {
    this.ec2Service = new AwsEc2Service();
  }

  public AwsEc2Scanner(Ec2Service ec2Service) {
    this.ec2Service = ec2Service;
  }

  @Override
  public String getProvider() {
    return "AWS";
  }

  @Override
  public String getServiceName() {
    return "AWS/EC2";
  }

  @Override
  public List<ResourceDetail> scan(CloudCredentials credentials) {
    return ec2Service.getAllEc2InstancesWithTags(credentials);
  }
}
