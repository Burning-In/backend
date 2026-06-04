package com.momentum.application;

import com.momentum.infrastructure.StockTickInfo;
import com.momentum.infrastructure.TickResponse;
import com.momentum.infrastructure.sse.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class StockRealtimeFacade {

  private static final String TICK_EVENT = "tick";

  private final SseEmitterRegistry sseEmitterRegistry;

  public SseEmitter subscribe(String stockCode) {
    return sseEmitterRegistry.create(stockCode);
  }

  public void broadcast(StockTickInfo tickInfo) {
    sseEmitterRegistry.broadcast(tickInfo.stockCode(), TICK_EVENT, TickResponse.from(tickInfo));
  }
}
