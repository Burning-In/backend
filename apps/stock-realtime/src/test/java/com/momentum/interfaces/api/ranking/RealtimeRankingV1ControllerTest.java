package com.momentum.interfaces.api.ranking;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.momentum.application.RealtimeRankingFacade;
import com.momentum.domain.score.StockRankScoreRepository;
import com.momentum.infrastructure.sse.SseEmitterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RealtimeRankingV1Controller.class)
@Import({RealtimeRankingFacade.class, SseEmitterRegistry.class})
class RealtimeRankingV1ControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private StockRankScoreRepository stockRankScoreRepository;

  @Test
  @DisplayName("돌파시작 랭킹 구독 시 비동기 SSE 연결이 시작된다")
  void subscribeBreakoutStartStartsSseStream() throws Exception {
    mockMvc.perform(get("/api/v1/realtime/ranking/breakout-start/subscribe"))
        .andExpect(status().isOk())
        .andExpect(request().asyncStarted());
  }

  @Test
  @DisplayName("돌파준비 랭킹 구독 시 비동기 SSE 연결이 시작된다")
  void subscribeBreakoutReadyStartsSseStream() throws Exception {
    mockMvc.perform(get("/api/v1/realtime/ranking/breakout-ready/subscribe"))
        .andExpect(status().isOk())
        .andExpect(request().asyncStarted());
  }
}
