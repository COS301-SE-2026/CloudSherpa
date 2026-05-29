package com.cloudsherpa.service.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudsherpa.service.sse.SseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class SseServiceTest {

  @Test
  void serviceSubscribeReturnsNonNull() {
    SseService service = new SseService();
    service.start();

    SseEmitter emitter = service.subscribe();
    assertNotNull(emitter);
  }

  // Lifecycle tests
  @Test
  void serviceIsRunningAfterStart() {
    SseService service = new SseService();
    service.start();

    assertTrue(service.isRunning());
  }

  @Test
  void serviceIsNotRunningAfterStop() {
    SseService service = new SseService();
    service.start();
    service.stop();

    assertFalse(service.isRunning());
  }

  @Test
  void subscribeReturnsNullAfterStop() {
    SseService service = new SseService();
    service.start();
    service.subscribe();
    service.stop();

    assertNull(service.subscribe());
  }

  // Broadcast tests. These are weak, but do not want to refactor SseService just to make
  // it easier to test. If broadcast ever needs to return indication of succesfull emit to
  // callers then we test properly
  @Test
  void broadCastReturnsWhenNotRunning() {
    SseService service = new SseService();
    service.stop();

    assertDoesNotThrow(() -> service.broadcast("event", "data"));
  }

  @Test
  void broadcastDoesNotThrowWhenValidArgs() {
    SseService service = new SseService();
    service.start();

    assertDoesNotThrow(() -> service.broadcast("event", "data"));
  }

  @Test
  void broadcastExceptionHandledInvalidArgs() {
    SseService service = new SseService();
    service.start();

    assertDoesNotThrow(() -> service.broadcast(null, "data"));
  }
}
