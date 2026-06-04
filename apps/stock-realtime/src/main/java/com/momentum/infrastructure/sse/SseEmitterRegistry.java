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

  private final Map<String, List<SseEmitter>> emittersByStockCode = new ConcurrentHashMap<>();

  public SseEmitter create(String stockCode) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MILLIS);

    List<SseEmitter> emitters =
        emittersByStockCode.computeIfAbsent(stockCode, code -> new CopyOnWriteArrayList<>());
    emitters.add(emitter);

    emitter.onCompletion(() -> remove(stockCode, emitter));
    emitter.onTimeout(() -> {
      log.debug("SSE 타임아웃 stockCode={}", stockCode);
      emitter.complete();
    });
    emitter.onError(e -> {
      log.debug("SSE 에러 stockCode={}", stockCode, e);
      remove(stockCode, emitter);
    });

    log.info("SSE 연결 등록 stockCode={}, 현재 구독자 수={}", stockCode, emitters.size());

    return emitter;
  }

  public void broadcast(String stockCode, String eventName, Object data) {
    List<SseEmitter> emitters = emittersByStockCode.get(stockCode);

    if (emitters == null || emitters.isEmpty()) {
      return;
    }

    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event()
            .name(eventName)
            .data(data));
      } catch (IOException | IllegalStateException e) {
        log.debug("SSE 전송 실패로 연결 제거 stockCode={}", stockCode, e);
        remove(stockCode, emitter);
      }
    }
  }

  private void remove(String stockCode, SseEmitter emitter) {
    List<SseEmitter> emitters = emittersByStockCode.get(stockCode);

    if (emitters == null) {
      return;
    }

    emitters.remove(emitter);

    if (emitters.isEmpty()) {
      emittersByStockCode.remove(stockCode, emitters);
    }
  }
}
