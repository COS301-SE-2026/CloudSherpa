package com.cloudsherpa.service.agenticdashboard.service;

import com.cloudsherpa.lib.entities.AiMessage;
import com.cloudsherpa.lib.repositories.AiMessageRepository;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiDashboardDraftService {

  private static final String DRAFT_PREFIX = "__CLOUDSHERPA_AI_DRAFT__:";

  private final AiMessageRepository messageRepository;
  private final ObjectMapper objectMapper;

  public AiDashboardDraftService(AiMessageRepository messageRepository, ObjectMapper objectMapper) {
    this.messageRepository = messageRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public Optional<DashboardPlanDto> findDraft(UUID sessionId) {
    List<AiMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

    for (int index = messages.size() - 1; index >= 0; index--) {
      String content = messages.get(index).getContent();
      if (content != null && content.startsWith(DRAFT_PREFIX)) {
        return Optional.of(readDraft(content.substring(DRAFT_PREFIX.length())));
      }
    }

    return Optional.empty();
  }

  @Transactional
  public DashboardPlanDto saveDraft(UUID sessionId, DashboardPlanDto draft) {
    deleteExistingDrafts(sessionId);

    String content = DRAFT_PREFIX + writeDraft(draft);

    messageRepository.save(
        AiMessage.builder()
            .messageId(UUID.randomUUID())
            .sessionId(sessionId)
            .role(AiMessage.AiMessageRole.ASSISTANT)
            .content(content)
            .createdAt(OffsetDateTime.now())
            .build());

    return draft;
  }

  @Transactional
  public void clearDraft(UUID sessionId) {
    deleteExistingDrafts(sessionId);
  }

  public boolean isDraftMessage(String content) {
    return content != null && content.startsWith(DRAFT_PREFIX);
  }

  private void deleteExistingDrafts(UUID sessionId) {
    List<AiMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

    messages.stream()
        .filter(message -> isDraftMessage(message.getContent()))
        .forEach(messageRepository::delete);
  }

  private String writeDraft(DashboardPlanDto draft) {
    try {
      return objectMapper.writeValueAsString(draft);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to persist the AI dashboard draft", exception);
    }
  }

  private DashboardPlanDto readDraft(String content) {
    try {
      return objectMapper.readValue(content, DashboardPlanDto.class);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Stored AI dashboard draft is invalid", exception);
    }
  }
}
