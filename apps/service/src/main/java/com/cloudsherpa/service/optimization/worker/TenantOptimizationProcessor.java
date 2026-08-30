package com.cloudsherpa.service.optimization.worker;

import com.cloudsherpa.service.config.TenantContext;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TenantOptimizationProcessor {

  private final TenantOptimizationWorker worker;

  public TenantOptimizationProcessor(TenantOptimizationWorker worker) {
    this.worker = worker;
  }

  public void process(UUID userId) {
    TenantContext.setCurrentTenant(userId.toString());

    try {

      worker.executeDatabaseWork(userId);

    } finally {

      TenantContext.clear();
    }
  }
}
