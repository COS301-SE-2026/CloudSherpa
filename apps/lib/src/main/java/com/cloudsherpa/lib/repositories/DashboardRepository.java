package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.Dashboard;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {

    List<Dashboard> findByUserId(UUID userId);
}