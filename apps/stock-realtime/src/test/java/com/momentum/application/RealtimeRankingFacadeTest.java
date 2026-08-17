package com.momentum.application;

import static com.momentum.sharedkernel.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.sharedkernel.StockRegime.DOWNSIDE_BREAK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momentum.application.dto.ranking.RealtimeBreakoutSuccessItem;
import com.momentum.infrastructure.query.RankedStockRow;
import com.momentum.infrastructure.query.RealtimeRankingQueryDao;
import com.momentum.infrastructure.sse.SseEmitterRegistry;
import com.momentum.sharedkernel.StockRegime;
import java.math.BigDecimal;
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
  private RealtimeRankingQueryDao realtimeRankingQueryDao;

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
  @DisplayName("updateRanking은 레짐 랭킹을 조회해 쿼리 정렬 순서 그대로 ranking-update 이벤트로 전송한다")
  @SuppressWarnings("unchecked")
  void updateRankingBroadcastsRegimeRanking() {
    // 정렬/필터/limit은 쿼리가 담당 → 이미 정렬된 순서로 반환된다고 가정
    when(realtimeRankingQueryDao.findRanked(eq(BREAKOUT_SUCCESS.name()), any(), anyInt()))
        .thenReturn(List.of(row("종목B", "000050"), row("종목A", "000040")));

    realtimeRankingFacade.updateRanking(BREAKOUT_SUCCESS);

    ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
    verify(sseEmitterRegistry).broadcast(eq("ranking-update:BREAKOUT_SUCCESS"), eq("ranking-update"), payload.capture());

    List<RealtimeBreakoutSuccessItem> response = (List<RealtimeBreakoutSuccessItem>) payload.getValue();
    assertThat(response).extracting(RealtimeBreakoutSuccessItem::stockCode)
        .containsExactly("000050", "000040");
    assertThat(response).extracting(RealtimeBreakoutSuccessItem::stockName)
        .containsExactly("종목B", "종목A");
  }

  @Test
  @DisplayName("랭킹 결과가 비어 있으면 broadcast하지 않는다")
  void updateRankingDoesNothingWhenEmpty() {
    when(realtimeRankingQueryDao.findRanked(eq(BREAKOUT_SUCCESS.name()), any(), anyInt()))
        .thenReturn(List.of());

    realtimeRankingFacade.updateRanking(BREAKOUT_SUCCESS);

    verify(sseEmitterRegistry, never()).broadcast(any(), any(), any());
  }

  @Test
  @DisplayName("랭킹 스트림이 없는 레짐이면 조회/전송하지 않는다")
  void updateRankingDoesNothingForUntrackedRegime() {
    realtimeRankingFacade.updateRanking(DOWNSIDE_BREAK);

    verify(realtimeRankingQueryDao, never()).findRanked(any(), any(), anyInt());
    verify(sseEmitterRegistry, never()).broadcast(any(), any(), any());
  }

  private RankedStockRow row(String name, String code) {
    return new RankedStockRow(name, code, BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.1));
  }
}
