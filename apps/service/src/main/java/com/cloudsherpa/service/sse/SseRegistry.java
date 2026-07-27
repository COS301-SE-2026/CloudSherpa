package com.cloudsherpa.service.sse;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class SseRegistry {
  private final Map<UUID, List<SseEmitter>> streamByUserId = new ConcurrentHashMap<>();

  public SseEmitter addStream(UUID userId) {
    List<SseEmitter> userStreams =
        streamByUserId.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>());
    SseEmitter emitter = new SseEmitter(0L);

    userStreams.add(emitter);
    emitter.onCompletion(() -> removeEmitter(userId, emitter));
    emitter.onTimeout(() -> removeEmitter(userId, emitter));
    emitter.onError(e -> removeEmitter(userId, emitter));

    return emitter;
  }

  public List<SseEmitter> getUserEmitters(UUID userId) {
    return streamByUserId.getOrDefault(userId, Collections.emptyList());
  }

  public void completeAllEmitters() {
    Collection<List<SseEmitter>> userEmitterCollections = streamByUserId.values();
    for (List<SseEmitter> userEmitters : userEmitterCollections) {
      for (SseEmitter emitter : userEmitters) {
        emitter.complete();
      }
    }
    streamByUserId.clear();
  }

  public void removeEmitter(UUID userId, SseEmitter emitter) {
    List<SseEmitter> userEmitters = streamByUserId.get(userId);
    if (userEmitters == null) {
      return;
    }

    userEmitters.remove(emitter);
    if (userEmitters.isEmpty()) {
      streamByUserId.remove(userId);
    }
  }
}
