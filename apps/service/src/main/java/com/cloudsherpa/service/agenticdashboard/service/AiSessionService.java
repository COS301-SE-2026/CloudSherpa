package com.cloudsherpa.service.agenticdashboard.service;

import com.cloudsherpa.lib.entities.AiSession;
import com.cloudsherpa.lib.repositories.AiSessionRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiSessionService {

  private final AiSessionRepository aiSessionRepository;

  public AiSessionService(AiSessionRepository aiSessionRepository) {
    this.aiSessionRepository = aiSessionRepository;
  }

  @Transactional
  public AiSession createSession(UUID userId) {
    OffsetDateTime now = OffsetDateTime.now();

    AiSession session =
        AiSession.builder()
            .sessionId(UUID.randomUUID())
            .userId(userId)
            .createdAt(now)
            .lastActivity(now)
            .currentVersionId(null)
            .build();

    return aiSessionRepository.save(session);
  }

  @Transactional(readOnly = true)
  public AiSession getSession(UUID userId, UUID sessionId) {
    return findSession(userId, sessionId);
  }

  @Transactional
  public void deleteSession(UUID userId, UUID sessionId) {
    AiSession session = findSession(userId, sessionId);

    aiSessionRepository.delete(session);
  }

  @Transactional
  public AiSession updateLastActivity(UUID userId, UUID sessionId) {
    AiSession session = findSession(userId, sessionId);

    session.setLastActivity(OffsetDateTime.now());

    return aiSessionRepository.save(session);
  }

  @Transactional
  public AiSession updateCurrentVersion(UUID userId, UUID sessionId, UUID versionId) {

    AiSession session = findSession(userId, sessionId);

    session.setCurrentVersionId(versionId);
    session.setLastActivity(OffsetDateTime.now());

    return aiSessionRepository.save(session);
  }

  private AiSession findSession(UUID userId, UUID sessionId) {
    return aiSessionRepository
        .findBySessionIdAndUserId(sessionId, userId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI session not found"));
  }
}
