package com.cloudsherpa.ingestion.billing;

import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.repositories.BillingExportConfigRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BillingExportConfigService {
  private final BillingExportConfigRepository billingExportConfigRepository;

  public BillingExportConfigService(BillingExportConfigRepository billingExportConfigRepository) {
    this.billingExportConfigRepository = billingExportConfigRepository;
  }

  public BillingExportConfig getAccountBillingExportConfig(UUID accountId) {
    return billingExportConfigRepository.findById(accountId).orElseThrow();
  }
}
