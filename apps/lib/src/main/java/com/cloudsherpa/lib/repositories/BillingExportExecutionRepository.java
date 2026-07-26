package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.BillingExportExecution;
import com.cloudsherpa.lib.entities.ExecutionStatusEnum;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingExportExecutionRepository extends JpaRepository<BillingExportExecution, UUID> {
  List<BillingExportExecution> findByConfigId(UUID configId);
  List<BillingExportExecution> findByStatus(ExecutionStatusEnum status);
  List<BillingExportExecution> findByConfigIdAndStatus(UUID configId, ExecutionStatusEnum status);
}