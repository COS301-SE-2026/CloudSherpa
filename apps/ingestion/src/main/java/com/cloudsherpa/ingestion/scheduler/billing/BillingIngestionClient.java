package com.cloudsherpa.ingestion.scheduler.billing;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.AwsCurIngestionService;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.GcpBillingIngestionService;
import com.cloudsherpa.lib.entities.ProviderEnum;
import org.springframework.stereotype.Service;

@Service
public class BillingIngestionClient {
  private final AwsCurIngestionService awsCurIngestionService;
  private final GcpBillingIngestionService gcpBillingIngestionService;

  public BillingIngestionClient(
      AwsCurIngestionService awsCurIngestionService,
      GcpBillingIngestionService gcpBillingIngestionService) {
    this.awsCurIngestionService = awsCurIngestionService;
    this.gcpBillingIngestionService = gcpBillingIngestionService;
  }

  public void execute(ProviderEnum provider, String userId, String configId) {

    switch (provider) {
      case AWS -> awsCurIngestionService.execute(userId, configId);
      case GCP -> gcpBillingIngestionService.execute(userId, configId);
      case AZURE -> throw new UnsupportedOperationException(
          "Azure billing ingestion not implemented yet");
    }
  }
}
