package com.cloudsherpa.service.optimization.worker;

import com.cloudsherpa.service.optimization.service.TenantService;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OptimizationWorker {

  private final TenantService tenantService;

  public OptimizationWorker(TenantService tenantService) {
    this.tenantService = tenantService;
  }

  @Scheduled(fixedRate = 86400000) // once every 24 hours in ms
  public void run() {
    for (UUID tenantId : tenantService.findTenantIds()) {
      processTenant(tenantId);
    }
  }

  void processTenant(UUID tenantId) {}
}
