package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.BillingForecastRun;
import com.cloudsherpa.lib.entities.ForecastExecutionStatusEnum;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BillingForecastRunRepository extends JpaRepository<BillingForecastRun, UUID> {
    // Pass status explicitly for hibernate mapping
    @Query("SELECT b FROM BillingForecastRun b WHERE b.forecastExecutionStatus = :status ORDER BY b.timestamp DESC, b.forecastRunId DESC LIMIT 1")
    public Optional<BillingForecastRun> latestForecastRun(ForecastExecutionStatusEnum status);
}
