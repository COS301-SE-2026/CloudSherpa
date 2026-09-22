package com.cloudsherpa.lib.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudsherpa.lib.entities.AzureBillingExportConfig;

public interface AzureBillingExportConfigRepository extends JpaRepository<AzureBillingExportConfig, UUID> {} 
