package com.cloudsherpa.lib.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudsherpa.lib.entities.BillingForecast;

public interface BillingForecastRepository extends JpaRepository<BillingForecast, UUID> {}
