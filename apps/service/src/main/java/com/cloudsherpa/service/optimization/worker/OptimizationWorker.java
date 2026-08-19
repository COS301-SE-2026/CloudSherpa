package com.cloudsherpa.service.optimization.worker;

import com.cloudsherpa.service.optimization.service.TenantService;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OptimizationWorker {

  private final TenantService tenantService;
  private final TenantOptimizationProcessor tenantOptimizationProcessor;

  public OptimizationWorker(
      TenantService tenantService, TenantOptimizationProcessor tenantOptimizationProcessor) {
    this.tenantOptimizationProcessor = tenantOptimizationProcessor;
    this.tenantService = tenantService;
  }

  @Scheduled(fixedRateString = "${optimization.worker.fixed-rate-ms}")
  public void run() {
    for (UUID tenantId : tenantService.findTenantIds()) {
      tenantOptimizationProcessor.process(tenantId);
    }
  }
}
