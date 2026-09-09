package com.cloudsherpa.ingestion.billing.provider.azure;

import com.cloudsherpa.ingestion.billing.BillingIngestionPipelineStep;
import com.cloudsherpa.ingestion.billing.BillingIngestionServiceInterface;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AzureBillingIngestionService implements BillingIngestionServiceInterface {

  private final List<BillingIngestionPipelineStep<AzureBillingContext>> azureBillingIngestionSteps;

  public AzureBillingIngestionService(
      List<BillingIngestionPipelineStep<AzureBillingContext>> azureBillingIngestionSteps) {
    this.azureBillingIngestionSteps = azureBillingIngestionSteps;
  }

  public void execute(String userId, String configId) {
    AzureBillingContext context =
        new AzureBillingContext(UUID.fromString(userId), UUID.fromString(configId));

    for (BillingIngestionPipelineStep<AzureBillingContext> step : azureBillingIngestionSteps) {
      step.execute(context);
    }
  }
}
