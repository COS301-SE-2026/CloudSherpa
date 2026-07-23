package com.cloudsherpa.service.scheduler;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.entities.OfferedMetric;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import com.cloudsherpa.lib.repositories.OfferedMetricRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.persistconnection.aws.dto.AwsCredentialsDto;
import com.cloudsherpa.service.persistconnection.aws.service.CredentialEncryptionService;
import com.cloudsherpa.service.scheduler.dto.AccountScope;
import com.cloudsherpa.service.scheduler.dto.CloudCredentials;
import com.cloudsherpa.service.scheduler.dto.IngestionRequestEvent;
import com.cloudsherpa.service.scheduler.dto.Instance;
import com.cloudsherpa.service.scheduler.dto.InstanceScope;
import com.cloudsherpa.service.scheduler.dto.Metric;
import com.cloudsherpa.service.scheduler.dto.ServiceScope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UsageIngestionService {
  private final UsageIngestionClient client;
  private final CloudAccountRepository cloudAccountRepository;
  private final CloudCredentialRepository cloudCredentialRepository;
  private final ResourceRepository resourceRepository;
  private final OfferedMetricRepository offeredMetricRepository;
  private final CredentialEncryptionService encryptionService;

  public UsageIngestionService(
      UsageIngestionClient client,
      CloudAccountRepository cloudAccountRepository,
      CloudCredentialRepository cloudCredentialRepository,
      ResourceRepository resourceRepository,
      OfferedMetricRepository offeredMetricRepository,
      CredentialEncryptionService encryptionService) {
    this.client = client;
    this.cloudAccountRepository = cloudAccountRepository;
    this.cloudCredentialRepository = cloudCredentialRepository;
    this.resourceRepository = resourceRepository;
    this.offeredMetricRepository = offeredMetricRepository;
    this.encryptionService = encryptionService;
  }

  @Transactional
  public void ingest(UUID accountId) {
    ObjectMapper mapper = new ObjectMapper();
    CloudAccount account =
        cloudAccountRepository
            .findById(accountId)
            .orElseThrow(
                () -> new IllegalArgumentException("Cloud account not found: " + accountId));
    CloudCredential credential = cloudCredentialRepository.findByAccountId(accountId).getFirst();
    String decryptedCredential = encryptionService.decrypt(credential.getCredentialValue());
    Instant ingestionEndTime = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    try {
      IngestionRequestEvent request = new IngestionRequestEvent();
      request.setFrom(account.getLastUsageIngestion().toInstant());
      request.setTo(ingestionEndTime);
      request.setPeriod(60);
      request.setIncludeUsage(true);
      request.setUserId(account.getConnection().getUser().getId());

      AccountScope accountScope = new AccountScope();
      accountScope.setAccountId(accountId.toString());
      accountScope.setProvider(account.getConnection().getProvider().toString());
      List<Resource> resources = resourceRepository.findByAccountId(accountId);

      List<ServiceScope> serviceScopes = new ArrayList<>();
      for (String serviceType :
          resources.stream().map(Resource::getResourceType).distinct().toList()) {
        ServiceScope serviceScope = new ServiceScope();
        serviceScope.setName(serviceType);
        List<Metric> metrics = new ArrayList<>();
        List<OfferedMetric> offeredMetrics =
            offeredMetricRepository.findByProviderAndServiceType(
                account.getConnection().getProvider(), serviceType);
        offeredMetrics.forEach(
            offeredMetric -> {
              Metric metric = new Metric();
              metric.setName(offeredMetric.getMetricName());
              metric.setUnit(offeredMetric.getExpectedUnit());
              metrics.add(metric);
            });
        serviceScope.setMetrics(metrics);
        List<Resource> serviceTypeResources =
            resources.stream()
                .filter(resource -> resource.getResourceType() == serviceType)
                .toList();

        List<Instance> instances = new ArrayList<>();
        serviceTypeResources.forEach(
            serviceTypeResource -> {
              Instance instance = new Instance();
              instance.setIdentifier(serviceTypeResource.getResourceIdentifier());
              instance.setRegion(serviceTypeResource.getRegion());
              instances.add(instance);
            });
        InstanceScope instanceScope = new InstanceScope();
        instanceScope.setIdentifierName(offeredMetrics.getFirst().getIdentifierField());
        instanceScope.setInstances(instances);

        serviceScopes.add(serviceScope);
      }
      accountScope.setServiceScopes(serviceScopes);
      List<AccountScope> accountScopes = new ArrayList<>();
      accountScopes.add(accountScope);
      request.setScopes(accountScopes);
      AwsCredentialsDto decryptedCredentialsDto =
          mapper.readValue(decryptedCredential, AwsCredentialsDto.class);
      CloudCredentials credentials = new CloudCredentials();
      credentials.setAccessKey(decryptedCredentialsDto.accessKeyId());
      credentials.setSecretKey(decryptedCredentialsDto.secretAccessKey());
      request.setCredentials(credentials);

      client.ingest(request);
      account.setLastUsageIngestion(ingestionEndTime.atOffset(ZoneOffset.UTC));

      account.setNextUsageIngestion(
          ingestionEndTime
              .atOffset(ZoneOffset.UTC)
              .plusSeconds(Long.parseLong(account.getIngestionPeriod())));

      cloudAccountRepository.save(account);

    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
