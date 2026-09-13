package com.cloudsherpa.ingestion.provider.gcp.services.cloudrun;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.google.cloud.asset.v1.ResourceSearchResult;
import com.google.cloud.run.v2.GetServiceRequest;
import com.google.cloud.run.v2.ServicesClient;
import org.springframework.stereotype.Service;

@Service
public class GcpCloudRunService implements CloudRunService {

  @Override
  public ResourceDetail getResourceDetail(
      ResourceSearchResult resource, CloudCredentials credentials) {

    CloudRunResourceIdentifier identifier =
        CloudRunResourceIdentifier.fromAssetName(resource.getName());

    String serviceName =
        String.format(
            "projects/%s/locations/%s/services/%s",
            identifier.projectId(), identifier.location(), identifier.serviceName());

    try (ServicesClient client = GcpClientFactory.createCloudRunClient(credentials)) {

      com.google.cloud.run.v2.Service service =
          client.getService(GetServiceRequest.newBuilder().setName(serviceName).build());

      return new ResourceDetail(
          service.getName(),
          identifier.serviceName(),
          "service_name",
          "cloud_run_service",
          identifier.location(),
          service.getLabelsMap());

    } catch (Exception e) {
      throw new IllegalStateException(
          "Unable to describe GCP Cloud Run service " + resource.getName(), e);
    }
  }
}
