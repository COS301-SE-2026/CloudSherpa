package com.cloudsherpa.ingestion.billing.provider.aws.cur;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline.AwsCurContext;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline.AwsCurIngestionPipelineStep;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AwsCurIngestionService {
  private final List<AwsCurIngestionPipelineStep> steps;

  public AwsCurIngestionService(List<AwsCurIngestionPipelineStep> steps) {
    this.steps = List.copyOf(steps);
  }

  public AwsCurContext execute() {
    AwsCurContext context = new AwsCurContext();

    for (AwsCurIngestionPipelineStep step : steps) {
      step.execute(context);
    }

    return context;
  }
}
