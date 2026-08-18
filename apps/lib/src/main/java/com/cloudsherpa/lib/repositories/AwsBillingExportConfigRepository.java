package com.cloudsherpa.lib.repositories;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudsherpa.lib.entities.AwsBillingExportConfig;


public interface AwsBillingExportConfigRepository extends JpaRepository<AwsBillingExportConfig, UUID> {}
