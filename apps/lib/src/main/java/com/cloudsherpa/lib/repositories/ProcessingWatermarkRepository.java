package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.ProcessingWatermark;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessingWatermarkRepository
    extends JpaRepository<ProcessingWatermark, UUID> {

  Optional<ProcessingWatermark> findByUserIdAndPipelineName(
      UUID userId, String pipelineName);
}