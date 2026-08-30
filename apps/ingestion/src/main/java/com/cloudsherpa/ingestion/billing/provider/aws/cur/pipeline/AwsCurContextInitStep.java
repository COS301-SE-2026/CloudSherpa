package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import com.cloudsherpa.ingestion.billing.BillingExportConfigService;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.scheduler.encryption.CredentialEncryptionService;
import com.cloudsherpa.lib.entities.AwsBillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportExecution;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.repositories.BillingExportExecutionRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

@Component
@Order(1)
public class AwsCurContextInitStep implements AwsCurIngestionPipelineStep {

  Logger logger = LoggerFactory.getLogger(AwsCurContextInitStep.class);
  private final BillingExportExecutionRepository billingExportExecutionRepository;
  private final BillingExportConfigService billingExportConfigService;
  private final String awsCurTmpDir;
  private final CloudCredentialRepository cloudCredentialRepository;
  private final CredentialEncryptionService encryptionService;

  public AwsCurContextInitStep(
      @Value("${sherpa.billing.aws.cur.tmp-dir}") String awsCurTmpDir,
      BillingExportExecutionRepository billingExportExecutionRepository,
      BillingExportConfigService billingExportConfigService,
      CloudCredentialRepository cloudCredentialRepository,
      CredentialEncryptionService encryptionService) {
    this.awsCurTmpDir = awsCurTmpDir;
    this.billingExportExecutionRepository = billingExportExecutionRepository;
    this.billingExportConfigService = billingExportConfigService;
    this.cloudCredentialRepository = cloudCredentialRepository;
    this.encryptionService = encryptionService;
  }

  @Override
  public void execute(AwsCurContext context) {
    // Query from DB with what is needed
    this.logger.info("Initializing AWS CUR Ingestion Pipeline Context");
    getProcessedExports(context);

    UUID configId = UUID.fromString(context.getConfigId());

    BillingExportConfig billingExportConfig =
        billingExportConfigService.getBillingExportConfig(configId);

    AwsBillingExportConfig awsBillingExportConfig =
        billingExportConfigService.getAccountAwsBillingExportConfig(configId);

    context.setBucketName(awsBillingExportConfig.getBucketName().strip());
    context.setBucketRegion(Region.of(awsBillingExportConfig.getBucketRegion()));
    context.setExportPrefix(awsBillingExportConfig.getExportPrefix());
    context.setExportName(awsBillingExportConfig.getExportName());
    context.setAccountId(billingExportConfig.getAccountId());
    context.setAwsCurTmpDir(Path.of(awsCurTmpDir));

    setCredentials(context);

    this.logger.info("Initialized AWS CUR Ingestion Pipeline Context");
  }

  // Doing this step in here for now, not sure if it has been done elsewhere. It
  // should definitely be done
  // outside of this component though
  private void setCredentials(AwsCurContext context) {
    List<CloudCredential> repoCloudCredentials =
        cloudCredentialRepository.findByAccountIdAndProvider(context.getAccountId(), "AWS");

    if (repoCloudCredentials.isEmpty()) {
      throw new IllegalStateException(
          "No AWS Credentials for account " + context.getAccountId().toString());
    }

    // Optimistically hope that there is one set of credentials per account
    CloudCredential credential = repoCloudCredentials.get(0);
    ObjectMapper objectMapper = new ObjectMapper();
    String encryptedCredentialValue = credential.getCredentialValue();
    String decryptedCredentialValue = encryptionService.decrypt(encryptedCredentialValue);
    try {
      CloudCredentials cloudCredentials =
          objectMapper.readValue(decryptedCredentialValue, CloudCredentials.class);
      context.setCredentials(cloudCredentials);
    } catch (JsonProcessingException jsonProcessingException) {
      throw new IllegalStateException(
          "Stored AWS credentials for account "
              + context.getAccountId().toString()
              + " are invalid");
    }
  }

  // This could problably be a bean and not be run on each export
  private void getProcessedExports(AwsCurContext context) {
    this.logger.info("Querying DB to get processed executions");

    List<BillingExportExecution> processedExportExecutions =
        billingExportExecutionRepository.findAll();

    for (BillingExportExecution processedExportExecution : processedExportExecutions) {
      context.addProcessedExport(processedExportExecution.getId().toString());
    }
  }
}
