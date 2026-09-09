package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline;

import com.cloudsherpa.ingestion.billing.BillingExportConfigService;
import com.cloudsherpa.ingestion.billing.BillingIngestionPipelineStep;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.scheduler.encryption.CredentialEncryptionService;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitStep implements BillingIngestionPipelineStep<AzureBillingContext> {

  private final CloudCredentialRepository cloudCredentialRepository;
  private final CredentialEncryptionService encryptionService;
  private final BillingExportConfigService billingExportConfigService;

  private Logger logger = LoggerFactory.getLogger(InitStep.class);

  public InitStep(
      CloudCredentialRepository cloudCredentialRepository,
      CredentialEncryptionService encryptionService,
      BillingExportConfigService billingExportConfigService) {
    this.cloudCredentialRepository = cloudCredentialRepository;
    this.encryptionService = encryptionService;
    this.billingExportConfigService = billingExportConfigService;
  }

  public void execute(AzureBillingContext context) {
    // Delay initializing the BillingExport until after when the manifests are discovered. Manifest
    // data is
    // required to construct the export id and prevent old exports from being re-ingested

    // Load config + credentials from DB + populate context
    setCredentials(context);
    setExportConfig(context);
  }

  private void setCredentials(AzureBillingContext context) {
    try {

      BillingExportConfig billingExportConfig =
          billingExportConfigService.getBillingExportConfig(context.getConfigId());

      List<CloudCredential> repoCloudCredentials =
          cloudCredentialRepository.findByAccountIdAndProvider(
              billingExportConfig.getAccountId(), "AZURE");

      if (repoCloudCredentials.isEmpty()) {
        throw new IllegalStateException(
            String.format(
                "No credentials found for user %s and account %s",
                context.getUserId(), billingExportConfig.getAccountId()));
      }

      ObjectMapper objectMapper = new ObjectMapper();
      CloudCredential credential = repoCloudCredentials.get(0);
      String decrypted = encryptionService.decrypt(credential.getCredentialValue());
      context.setCredentials(objectMapper.readValue(decrypted, CloudCredentials.class));
    } catch (JsonProcessingException | NoSuchElementException e) {
      throw new IllegalStateException(e);
    }
  }

  private void setExportConfig(AzureBillingContext context) {
    try {
      context.setExportConfig(
          billingExportConfigService.getAzureBillingExportConfig(context.getConfigId()));
    } catch (NoSuchElementException e) {
      logger.error(
          "Azure Billing Export Config with Config ID {} does not exist for user {}",
          context.getConfigId(),
          context.getUserId());
      throw new IllegalStateException(e);
    }
  }
}
