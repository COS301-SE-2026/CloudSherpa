package com.cloudsherpa.ingestion.provider.aws.services.ec2;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.ec2.model.Instance;

public interface Ec2Service {
  public List<Instance> getAllEc2Instances(CloudCredentials credentials);

  public Map<String, String> getTagsForInstance(Instance instance);

  public List<ResourceDetail> getAllEc2InstancesWithTags(CloudCredentials credentials);
}
