package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.GcpBillingExportConfig;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GcpBillingExportConfigRepository extends JpaRepository<GcpBillingExportConfig, UUID> {}
