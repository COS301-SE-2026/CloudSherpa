package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.BillingExportConfigService;
import com.cloudsherpa.ingestion.billing.BillingExportService;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.cloudsherpa.ingestion.scheduler.encryption.CredentialEncryptionService;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.entities.GcpBillingExportConfig;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.bigquery.BigQuery;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class GcpBillingInitStep implements GcpBillingIngestionStep {

  @Value("${dev.gcp.billing_dev:false}")
  private boolean devConfig;

  @Value("${dev.gcp.project_id:}")
  private String devProjectId;

  @Value("${dev.gcp.dataset_id:}")
  private String devDatasetId;

  @Value("${dev.gcp.billing_account_id:}")
  private String devBillingAccountId;

  @Value("${dev.gcp.service_account_json_path:}")
  private String devServiceAccountPath;

  private final Logger logger = LoggerFactory.getLogger(GcpBillingInitStep.class);
  private final BillingExportConfigService billingExportConfigService;
  private final BillingExportService billingExportService;
  private final CloudCredentialRepository cloudCredentialRepository;
  private final CredentialEncryptionService encryptionService;

  public GcpBillingInitStep(
      BillingExportConfigService billingExportConfigService,
      BillingExportService billingExportService,
      CloudCredentialRepository cloudCredentialRepository,
      CredentialEncryptionService encryptionService) {
    this.billingExportConfigService = billingExportConfigService;
    this.billingExportService = billingExportService;
    this.cloudCredentialRepository = cloudCredentialRepository;
    this.encryptionService = encryptionService;
  }

  public void execute(GcpBillingContext context) {

    CloudCredentials cloudCredentials;

    if (devConfig) {
      logger.info("GCP Billing Ingestion Dev Mode enabled ");
      if (devProjectId.isBlank() || devDatasetId.isBlank() || devBillingAccountId.isBlank()) {
        throw new IllegalStateException(
            "Dev GCP billing ingestion config enabled but configuration values are missing");
      }

      BillingExportConfig billingExportConfig =
          new BillingExportConfig(
              context.getConfigId(),
              UUID.fromString(devBillingAccountId),
              OffsetDateTime.now(ZoneId.of("UTC")));
      billingExportConfigService.saveBillingExport(billingExportConfig);
      GcpBillingExportConfig gcpBillingExportConfig =
          new GcpBillingExportConfig(
              billingExportConfig.getId(), devDatasetId, devBillingAccountId);
      billingExportConfigService.saveGcpBillingExport(gcpBillingExportConfig);

      GcpBillingConfig devGcpBillingConfig =
          new GcpBillingConfig(devProjectId, devDatasetId, devBillingAccountId);

      cloudCredentials = getGcpDevCredentials();

      context.setGcpBillingConfig(devGcpBillingConfig);
      context.setCloudCredentials(cloudCredentials);
      context.setBigQueryClient(getBigQueryClient(cloudCredentials));
    } else {
      cloudCredentials = getGcpAccountCloudCredentials(null);
    }

    // Common behavior for both dev and other configurations
    BillingExportConfig billingExportConfig =
        billingExportConfigService.getBillingExportConfig(context.getConfigId());
    GcpBillingExportConfig gcpBillingExportConfig =
        billingExportConfigService.getGcpBillingExportConfig(context.getConfigId());
    BillingExport billingExport =
        billingExportService.initializeExport(
            billingExportConfig.getId().toString(), context.getConfigId().toString(), null);

    context.setBillingExport(billingExport);

    context.setGcpBillingConfig(
        new GcpBillingConfig(
            cloudCredentials.getProjectId(),
            gcpBillingExportConfig.getDatasetId(),
            gcpBillingExportConfig.getBillingAccountId()));

    Instant queryFrom =
        Instant.now()
            .minusSeconds(
                (long) 84_000 * 3); // temporarily set queryFrom to 3 days before time of ingestion
    context.setQueryFrom(queryFrom);
  }

  private CloudCredentials getGcpAccountCloudCredentials(UUID accountId) {
    List<CloudCredential> repoCloudCredentials =
        cloudCredentialRepository.findByAccountIdAndProvider(accountId, "GCP");
    ObjectMapper objectMapper = new ObjectMapper();
    CloudCredential credential = repoCloudCredentials.get(0);
    String decrypted = encryptionService.decrypt(credential.getCredentialValue());
    try {
      return objectMapper.readValue(decrypted, CloudCredentials.class);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private CloudCredentials getGcpDevCredentials() {
    if (!devConfig) {
      logger.error(
          "GCP dev flag set to {} but an attempt was made to obtain dev credentials", devConfig);
      throw new IllegalStateException(
          "Cannot use dev GCP credentials when devConfig flag is not set to true");
    }

    try {
      String json = Files.readString(Path.of(devServiceAccountPath));
      CloudCredentials gcpCloudCredentials = new CloudCredentials();
      gcpCloudCredentials.setServiceAccountJson(json);
      return gcpCloudCredentials;
    } catch (IOException ioException) {
      logger.error("Failed to load dev service account file at {}", devServiceAccountPath);
      throw new IllegalStateException("Could not load dev service account file", ioException);
    }
  }

  private BigQuery getBigQueryClient(CloudCredentials credentials) {
    try {
      return GcpClientFactory.createBigQueryClient(credentials);
    } catch (IOException ioException) {
      throw new IllegalStateException("Failed to obtain BigQuery client", ioException);
    }
  }
}
