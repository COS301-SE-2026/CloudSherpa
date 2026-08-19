package com.cloudsherpa.ingestion.unit.scheduling.billing;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.AwsCurIngestionService;
import com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.GcpBillingIngestionService;
import com.cloudsherpa.ingestion.scheduler.billing.BillingIngestionClient;
import com.cloudsherpa.lib.entities.ProviderEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingIngestionClientTest {
  @Mock AwsCurIngestionService awsCurIngestionService;

  @Mock GcpBillingIngestionService gcpBillingIngestionService;

  private BillingIngestionClient billingIngestionClient;

  private static final String VALID_USER_ID = "6c2add6c-2609-4615-b2c2-0d2aee8ff80e";
  private static final String VALID_CONFIG_ID = "57cc79ef-9788-4862-ae37-61dc7569f993";

  @BeforeEach
  void setUp() {
    billingIngestionClient =
        new BillingIngestionClient(awsCurIngestionService, gcpBillingIngestionService);
  }

  @Test
  void shouldCallCorrectAwsBillingIngestionService() {
    // act
    billingIngestionClient.execute(ProviderEnum.AWS, VALID_USER_ID, VALID_CONFIG_ID);

    // assert
    verify(awsCurIngestionService).execute(VALID_USER_ID, VALID_CONFIG_ID);
    verify(gcpBillingIngestionService, never()).execute(any(), any());
  }

  @Test
  void shouldCallCorectGcpBillingIngestionService() {
    // act
    billingIngestionClient.execute(ProviderEnum.GCP, VALID_USER_ID, VALID_CONFIG_ID);

    // assert
    verify(gcpBillingIngestionService).execute(VALID_USER_ID, VALID_CONFIG_ID);
    verify(awsCurIngestionService, never()).execute(any(), any());
  }

  @Test
  void awsBillingIngestionShouldThrowWhileUnsupported() {
    // act & assert
    assertThatThrownBy(
            () ->
                billingIngestionClient.execute(ProviderEnum.AZURE, VALID_USER_ID, VALID_CONFIG_ID))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
