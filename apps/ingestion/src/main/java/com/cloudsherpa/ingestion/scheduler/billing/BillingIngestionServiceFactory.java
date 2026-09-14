package com.cloudsherpa.ingestion.scheduler.billing;

import com.cloudsherpa.ingestion.billing.BillingIngestionService;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BillingIngestionServiceFactory {
  private final Map<String, BillingIngestionService> services;

  public BillingIngestionServiceFactory(Map<String, BillingIngestionService> services) {
    this.services = services;
  }

  public BillingIngestionService get(String serviceKey) {
    BillingIngestionService service = services.get(serviceKey);

    if (service == null) {
      throw new IllegalStateException("Unsupported serviceKey");
    }

    return service;
  }
}
