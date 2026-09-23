package com.cloudsherpa.lib.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudsherpa.lib.entities.BillingForecastExecutionLog;

public interface BillingForecastExecutionLogRepository extends JpaRepository<BillingForecastExecutionLog, UUID> {}
