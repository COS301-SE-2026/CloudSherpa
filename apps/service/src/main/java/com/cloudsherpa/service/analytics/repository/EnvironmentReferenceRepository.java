package com.cloudsherpa.service.analytics.repository;

import com.cloudsherpa.service.analytics.entity.EnvironmentReference;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// Needs to know which table it is managing and what the datatype is for its id field
public interface EnvironmentReferenceRepository extends JpaRepository<EnvironmentReference, UUID> {
  // Interface instantly inherits dozens of pre-written database commands
  // Can also add custom queries using method naming (optional)
}
