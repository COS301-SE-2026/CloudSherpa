package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.AiMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiMessageRepository extends JpaRepository<AiMessage, UUID> {

  List<AiMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}
