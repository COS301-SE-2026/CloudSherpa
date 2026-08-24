package com.cloudsherpa.ingestion.provider.aws.services.ec2;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.scanner.ResourceScanner;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AwsEc2Scanner implements ResourceScanner {
  private final Ec2Service ec2Service;
  private final Ec2PermissionsService permissionsService;

  public AwsEc2Scanner(Ec2Service ec2Service, Ec2PermissionsService permissionsService) {
    this.ec2Service = ec2Service;
    this.permissionsService = permissionsService;
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

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }
}
