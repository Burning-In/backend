package com.momentum.application;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.domain.stock.StockRegime.DOWNSIDE_BREAK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momentum.application.dto.ranking.RealtimeBreakoutSuccessItem;
import com.momentum.domain.score.FipScore;
import com.momentum.domain.score.Momentum;
import com.momentum.domain.score.StockRankScore;
import com.momentum.domain.score.StockRankScoreRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockTrend;
import com.momentum.infrastructure.sse.SseEmitterRegistry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class RealtimeRankingFacadeTest {

  @Mock
  private SseEmitterRegistry sseEmitterRegistry;

  @Mock
  private StockRankScoreRepository stockRankScoreRepository;

  @InjectMocks
  private RealtimeRankingFacade realtimeRankingFacade;

  @Test
  @DisplayName("subscribeBreakoutSuccess는 돌파성공 key로 SSE 연결을 생성해 반환한다")
  void subscribeBreakoutSuccessDelegatesToRegistry() {
    SseEmitter emitter = new SseEmitter();
    when(sseEmitterRegistry.create("ranking-update:BREAKOUT_SUCCESS")).thenReturn(emitter);

    SseEmitter result = realtimeRankingFacade.subscribeBreakoutSuccess();

    assertThat(result).isSameAs(emitter);
    verify(sseEmitterRegistry).create("ranking-update:BREAKOUT_SUCCESS");
  }

  @Test
  @DisplayName("subscribeBreakoutReady는 돌파준비 key로 SSE 연결을 생성해 반환한다")
  void subscribeBreakoutReadyDelegatesToRegistry() {
    SseEmitter emitter = new SseEmitter();
    when(sseEmitterRegistry.create("ranking-update:BREAKOUT_READY")).thenReturn(emitter);

    SseEmitter result = realtimeRankingFacade.subscribeBreakoutReady();

    assertThat(result).isSameAs(emitter);
    verify(sseEmitterRegistry).create("ranking-update:BREAKOUT_READY");
  }

  @Test
  @DisplayName("updateRanking은 레짐 랭킹을 조회해 리포지토리 정렬 순서 그대로 ranking-update 이벤트로 전송한다") // 1차 필터, 2차 필터 검증 필요
  @SuppressWarnings("unchecked")
  void updateRankingBroadcastsRegimeRanking() {
    // 정렬/필터/limit은 리포지토리(쿼리)가 담당 → 이미 정렬된 순서로 반환된다고 가정
    StockRankScore first = score("종목B", "000002", BREAKOUT_SUCCESS, "0.30", "0.50");
    StockRankScore second = score("종목A", "000001", BREAKOUT_SUCCESS, "0.10", "0.20");
    when(stockRankScoreRepository.findLastStockRankScore(eq(BREAKOUT_SUCCESS), any(), anyLong()))
        .thenReturn(List.of(first, second));

    realtimeRankingFacade.updateRanking(BREAKOUT_SUCCESS);

    ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
    verify(sseEmitterRegistry).broadcast(eq("ranking-update:BREAKOUT_SUCCESS"), eq("ranking-update"), payload.capture());

    List<RealtimeBreakoutSuccessItem> response = (List<RealtimeBreakoutSuccessItem>) payload.getValue();
    assertThat(response).extracting(RealtimeBreakoutSuccessItem::stockCode)
        .containsExactly("000002", "000001");
  }

  @Test
  @DisplayName("랭킹 결과가 비어 있으면 broadcast하지 않는다")
  void updateRankingDoesNothingWhenEmpty() {
    when(stockRankScoreRepository.findLastStockRankScore(eq(BREAKOUT_SUCCESS), any(), anyLong()))
        .thenReturn(List.of());

    realtimeRankingFacade.updateRanking(BREAKOUT_SUCCESS);

    verify(sseEmitterRegistry, never()).broadcast(any(), any(), any());
  }

  @Test
  @DisplayName("랭킹 스트림이 없는 레짐이면 조회/전송하지 않는다")
  void updateRankingDoesNothingForUntrackedRegime() {
    realtimeRankingFacade.updateRanking(DOWNSIDE_BREAK);

    verify(stockRankScoreRepository, never()).findLastStockRankScore(any(), any(), anyLong());
    verify(sseEmitterRegistry, never()).broadcast(any(), any(), any());
  }

  private StockRankScore score(String name, String code, StockRegime regime, String momentum, String fip) {
    Stock stock = new Stock(name, code, regime, StockTrend.UPTREND);
    return StockRankScore.create(
        Momentum.of(new BigDecimal(momentum)),
        FipScore.of(new BigDecimal(fip), 100, 50),
        LocalDate.now(),
        stock);
  }
}
