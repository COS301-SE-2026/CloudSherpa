package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.ProcessingWatermark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessingWatermarkRepository
    extends JpaRepository<ProcessingWatermark, String> {}