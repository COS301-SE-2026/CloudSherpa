package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.BillingForecastRun;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingForecastRunRepository extends JpaRepository<BillingForecastRun, UUID> {}
