package com.cloudsherpa.ingestion.unit.scheduling.billing;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.ingestion.billing.BillingIngestionService;
import com.cloudsherpa.ingestion.scheduler.billing.BillingIngestionClient;
import com.cloudsherpa.ingestion.scheduler.billing.BillingIngestionServiceFactory;
import com.cloudsherpa.lib.entities.ProviderEnum;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingIngestionClientTest {
  @Mock BillingIngestionServiceFactory billingIngestionServiceFactory;
  @Mock BillingIngestionService billingIngestionService;
  @InjectMocks private BillingIngestionClient billingIngestionClient;

  private static final String VALID_USER_ID = "6c2add6c-2609-4615-b2c2-0d2aee8ff80e";
  private static final String VALID_CONFIG_ID = "57cc79ef-9788-4862-ae37-61dc7569f993";

  @ParameterizedTest
  @CsvSource({
    "AWS, awsBillingIngestionService",
    "AZURE, azureBillingIngestionService",
    "GCP, gcpBillingIngestionService"
  })
  void shouldConstructCorrectServiceKey(ProviderEnum provider, String expectedServiceKey) {
    mockFactory(expectedServiceKey);
    billingIngestionClient.execute(provider, VALID_USER_ID, VALID_CONFIG_ID);

    verify(billingIngestionServiceFactory).get(expectedServiceKey);
  }

  private void mockFactory(String expectedServiceKey) {
    when(billingIngestionServiceFactory.get(expectedServiceKey))
        .thenReturn(billingIngestionService);
  }
}
