package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.AiSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiSessionRepository extends JpaRepository<AiSession, UUID> {

  Optional<AiSession> findBySessionIdAndUserId(UUID sessionId, UUID userId);
}
