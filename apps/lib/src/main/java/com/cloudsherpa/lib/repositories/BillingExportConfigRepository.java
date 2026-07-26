package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.BillingExportConfig;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingExportConfigRepository extends JpaRepository<BillingExportConfig, UUID> {
  List<BillingExportConfig> findByAccountId(UUID accountId);
}