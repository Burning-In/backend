package com.momentum.infrastructure.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class SseEmitterRegistry {

  private static final long DEFAULT_TIMEOUT_MILLIS = 60 * 60 * 1000L;

  private final Map<String, List<SseEmitter>> emittersByKey = new ConcurrentHashMap<>();

  public SseEmitter create(String key) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MILLIS);

    List<SseEmitter> emitters =
        emittersByKey.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
    emitters.add(emitter);

    emitter.onCompletion(() -> remove(key, emitter));
    emitter.onTimeout(() -> {
      log.debug("SSE 타임아웃 key={}", key);
      emitter.complete();
    });
    emitter.onError(e -> {
      log.debug("SSE 에러 key={}", key, e);
      remove(key, emitter);
    });

    log.info("SSE 연결 등록 key={}, 현재 구독자 수={}", key, emitters.size());

    return emitter;
  }

  public void broadcast(String key, String eventName, Object data) {
    List<SseEmitter> emitters = emittersByKey.get(key);

    if (emitters == null || emitters.isEmpty()) {
      return;
    }

    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event()
            .name(eventName)
            .data(data));
      } catch (IOException | IllegalStateException e) {
        log.debug("SSE 전송 실패로 연결 제거 key={}", key, e);
        remove(key, emitter);
      }
    }
  }

  private void remove(String key, SseEmitter emitter) {
    List<SseEmitter> emitters = emittersByKey.get(key);

    if (emitters == null) {
      return;
    }

    emitters.remove(emitter);

    if (emitters.isEmpty()) {
      emittersByKey.remove(key, emitters);
    }
  }
}
