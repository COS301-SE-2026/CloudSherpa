package com.cloudsherpa.service.sse;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseService implements SmartLifecycle {

  private final Logger logger = LoggerFactory.getLogger(SseService.class);
  private volatile boolean running;
  private final SseRegistry sseRegistry;

  public SseService() {
    sseRegistry = new SseRegistry();
  }

  public SseEmitter subscribe(UUID userId) {
    if (!running) {
      return null;
    }

    return sseRegistry.addStream(userId);
  }

  public void broadcast(UUID userId, String eventName, Object data) {
    if (!running) {
      return;
    }
    for (SseEmitter emitter : sseRegistry.getUserEmitters(userId)) {
      try {
        if (Objects.nonNull(data) && Objects.nonNull(eventName)) {
          emitter.send(SseEmitter.event().name(eventName).data(data));
        } else {
          logger.warn("Invalid SSE event:\nEvent Name: {}\nData: {}", eventName, data);
        }
      } catch (IOException e) {
        logger.error(
            String.format(
                "SSE emitter error. Closing emitter. Error message:%n%s", e.getMessage()));
        emitter.complete();
        sseRegistry.removeEmitter(userId, emitter);
      }
    }
  }

  @Override
  public void stop() {
    stop(() -> {});
  }

  @Override
  public void stop(Runnable callback) {
    running = false;
    sseRegistry.completeAllEmitters();
    callback.run();
  }

  @Override
  public void start() {
    running = true;
  }

  @Override
  public boolean isRunning() {
    return running;
  }
}
