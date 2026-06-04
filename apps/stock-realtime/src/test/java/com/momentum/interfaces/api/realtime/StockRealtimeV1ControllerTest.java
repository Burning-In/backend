package com.momentum.interfaces.api.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.momentum.application.StockRealtimeFacade;
import com.momentum.infrastructure.StockTickInfo;
import com.momentum.infrastructure.sse.SseEmitterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(StockRealtimeV1Controller.class)
@Import({StockRealtimeFacade.class, SseEmitterRegistry.class})
class StockRealtimeV1ControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private StockRealtimeFacade stockRealtimeFacade;

  @Test
  @DisplayName("틱 구독 요청 시 비동기 SSE 연결이 시작된다")
  void subscribeTickStartsSseStream() throws Exception {
    mockMvc.perform(get("/api/v1/stocks/005930/realtime/tick"))
        .andExpect(status().isOk())
        .andExpect(request().asyncStarted());
  }

  @Test
  @DisplayName("구독 이후 broadcast된 틱이 text/event-stream의 tick 이벤트로 전달된다")
  void broadcastedTickIsStreamedToSubscriber() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/api/v1/stocks/005930/realtime/tick"))
        .andExpect(request().asyncStarted())
        .andReturn();

    StockTickInfo tickInfo = new StockTickInfo(
        "005930", "090000", 70_000L, "2", 1_000L, 1.45,
        69_500.0, 69_000L, 71_000L, 68_500L, 70_100L, 69_900L,
        10L, 1_234_567L, 120.5);
    stockRealtimeFacade.broadcast(tickInfo);

    assertThat(mvcResult.getResponse().getContentType())
        .contains(MediaType.TEXT_EVENT_STREAM_VALUE);

    String content = mvcResult.getResponse().getContentAsString();
    assertThat(content).contains("event:tick");
    assertThat(content).contains("005930");
  }
}
