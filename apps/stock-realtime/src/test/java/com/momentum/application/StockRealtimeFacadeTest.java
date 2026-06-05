package com.momentum.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momentum.application.dto.StockTickInfo;
import com.momentum.application.dto.TickResponse;
import com.momentum.infrastructure.sse.SseEmitterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class StockRealtimeFacadeTest {

  @Mock
  private SseEmitterRegistry sseEmitterRegistry;

  @InjectMocks
  private StockRealtimeFacade stockRealtimeFacade;

  @Test
  @DisplayName("subscribe는 종목코드로 SSE 연결을 생성해 그대로 반환한다")
  void subscribeDelegatesToRegistry() {
    SseEmitter emitter = new SseEmitter();
    when(sseEmitterRegistry.create("005930")).thenReturn(emitter);

    SseEmitter result = stockRealtimeFacade.subscribe("005930");

    assertThat(result).isSameAs(emitter);
    verify(sseEmitterRegistry).create("005930");
  }

  @Test
  @DisplayName("broadcast는 틱을 TickResponse로 변환해 종목코드 key의 tick 이벤트로 전송한다")
  void broadcastConvertsAndSends() {
    StockTickInfo tickInfo = new StockTickInfo(
        "005930", "090000", 70_000L, "2", 1_000L, 1.45,
        69_500.0, 69_000L, 71_000L, 68_500L, 70_100L, 69_900L,
        10L, 1_234_567L, 120.5);

    stockRealtimeFacade.broadcast(tickInfo);

    verify(sseEmitterRegistry).broadcast("005930", "tick", TickResponse.from(tickInfo));
  }
}
