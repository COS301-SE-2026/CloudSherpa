package com.cloudsherpa.service.sse;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseService implements SmartLifecycle {

  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
  private final Logger logger = LoggerFactory.getLogger(SseService.class);
  private volatile boolean running;

  public SseEmitter subscribe() {
    if (!running) {
      return null;
    }
    SseEmitter emitter = new SseEmitter(0L);

    emitters.add(emitter);

    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError(e -> emitters.remove(emitter));

    return emitter;
  }

  public void broadcast(String eventName, Object data) {
    if (!running) {
      return;
    }
    for (SseEmitter emitter : emitters) {
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
        emitters.remove(emitter);
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

    for (SseEmitter emitter : emitters) {
      emitter.complete();
    }

    emitters.clear();
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
