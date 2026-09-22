package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.AiDashboardVersion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiDashboardVersionRepository
    extends JpaRepository<AiDashboardVersion, UUID> {

  List<AiDashboardVersion> findBySessionIdOrderByVersionNumberDesc(
      UUID sessionId);

  Optional<AiDashboardVersion> findByVersionIdAndSessionId(
      UUID versionId,
      UUID sessionId);

  Optional<AiDashboardVersion> findTopBySessionIdOrderByVersionNumberDesc(
      UUID sessionId);
}
