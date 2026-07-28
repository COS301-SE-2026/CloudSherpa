package com.cloudsherpa.ingestion.scheduler.billing;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.AwsCurIngestionService;
import org.springframework.stereotype.Service;

@Service
public class BillingIngestionClient {
  private final AwsCurIngestionService awsCurIngestionService;

  public BillingIngestionClient(AwsCurIngestionService awsCurIngestionService) {
    this.awsCurIngestionService = awsCurIngestionService;
  }

  public void execute(String userId, String configId) {
    awsCurIngestionService.execute(userId, configId);
  }
}
