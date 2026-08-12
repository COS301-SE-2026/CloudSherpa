package com.cloudsherpa.ingestion.provider.gcp.services.compute;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.google.cloud.asset.v1.Asset;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import org.springframework.stereotype.Service;

@Service
public class GcpComputeService implements ComputeService {

  @Override
  public ResourceDetail getResourceDetail(Asset asset, CloudCredentials credentials) {
    ComputeResourceIdentifier identifier = ComputeResourceIdentifier.fromAssetName(asset.getName());

    try (InstancesClient client = InstancesClient.create()) {

      Instance instance =
          client.get(identifier.projectId(), identifier.zone(), identifier.instanceName());

      return new ResourceDetail(
          Long.toString(instance.getId()),
          instance.getName(),
          "instance_id",
          "gce_instance",
          identifier.zone(),
          instance.getLabelsMap());

    } catch (Exception e) {
      throw new IllegalStateException(
          "Unable to describe GCP Compute Engine instance " + asset.getName(), e);
    }
  }
}
