package com.cloudsherpa.ingestion.normalization.persistence.repository;

import com.cloudsherpa.ingestion.normalization.persistence.entity.EnvironmentReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// Needs to know which table it is managing and what the datatype is for its id field
public interface EnvironmentReferenceRepository extends JpaRepository<EnvironmentReference, UUID> 
{
    // Interface instantly inherits dozens of pre-written database commands
    // Can also add custom queries using method naming (optional)
}
