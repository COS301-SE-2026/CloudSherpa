package com.cloudsherpa.ingestion.scheduler.billing;

import com.cloudsherpa.ingestion.billing.BillingIngestionService;
import com.cloudsherpa.lib.entities.ProviderEnum;
import org.springframework.stereotype.Service;

@Service
public class BillingIngestionClient {
  private final BillingIngestionServiceFactory billingIngestionServiceFactory;

  public BillingIngestionClient(BillingIngestionServiceFactory billingIngestionServiceFactory) {
    this.billingIngestionServiceFactory = billingIngestionServiceFactory;
  }

  public void execute(ProviderEnum provider, String userId, String configId) {
    String serviceKey = provider.toString().toLowerCase() + "BillingIngestionService";
    BillingIngestionService service = billingIngestionServiceFactory.get(serviceKey);
    service.execute(userId, configId);
  }
}
