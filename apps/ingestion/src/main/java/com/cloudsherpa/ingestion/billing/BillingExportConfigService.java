package com.cloudsherpa.ingestion.billing;

import com.cloudsherpa.lib.entities.AwsBillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.repositories.AwsBillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.BillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.GcpBillingExportConfigRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BillingExportConfigService {
  private final BillingExportConfigRepository billingExportConfigRepository;
  private final AwsBillingExportConfigRepository awsBillingExportConfigRepository;
  private final GcpBillingExportConfigRepository gcpBillingExportConfigRepository;

  public BillingExportConfigService(
      BillingExportConfigRepository billingExportConfigRepository,
      AwsBillingExportConfigRepository awsBillingExportConfigRepository,
      GcpBillingExportConfigRepository gcpBillingExportConfigRepository) {
    this.billingExportConfigRepository = billingExportConfigRepository;
    this.awsBillingExportConfigRepository = awsBillingExportConfigRepository;
    this.gcpBillingExportConfigRepository = gcpBillingExportConfigRepository;
  }

  public BillingExportConfig getBillingExportConfig(UUID configId) {
    return billingExportConfigRepository.findById(configId).orElseThrow();
  }

  public AwsBillingExportConfig getAccountAwsBillingExportConfig(UUID configId) {
    BillingExportConfig config = billingExportConfigRepository.findById(configId).orElseThrow();
    return awsBillingExportConfigRepository.findById(config.getId()).orElseThrow();
  }
}
