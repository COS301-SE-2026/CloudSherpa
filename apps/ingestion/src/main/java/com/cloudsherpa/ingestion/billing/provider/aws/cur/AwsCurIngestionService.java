package com.cloudsherpa.ingestion.billing.provider.aws.cur;

import com.cloudsherpa.ingestion.billing.BillingIngestionServiceInterface;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline.AwsCurContext;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline.AwsCurIngestionPipelineStep;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AwsCurIngestionService implements BillingIngestionServiceInterface {
  private final List<AwsCurIngestionPipelineStep> steps;

  public AwsCurIngestionService(List<AwsCurIngestionPipelineStep> steps) {
    this.steps = List.copyOf(steps);
  }

  public void execute(String userId, String configId) {
    AwsCurContext context = new AwsCurContext(userId, configId);

    for (AwsCurIngestionPipelineStep step : steps) {
      step.execute(context);
    }
  }
}
