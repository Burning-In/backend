package com.momentum.application;

import static com.momentum.sharedkernel.StockRegime.BREAKOUT_READY;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_SUCCESS;

import com.momentum.application.dto.ranking.RealtimeBreakoutReadyItem;
import com.momentum.application.dto.ranking.RealtimeBreakoutSuccessItem;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.infrastructure.query.RankedStockRow;
import com.momentum.infrastructure.query.RealtimeRankingQueryDao;
import com.momentum.infrastructure.sse.SseEmitterRegistry;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class RealtimeRankingFacade {

  private static final String RANKING_UPDATE_PREFIX = "ranking-update:";
  private static final String BREAKOUT_SUCCESS_RANKING_KEY = RANKING_UPDATE_PREFIX + BREAKOUT_SUCCESS;
  private static final String BREAKOUT_READY_RANKING_KEY = RANKING_UPDATE_PREFIX + BREAKOUT_READY;

  private static final String RANKING_EVENT = "ranking-update";
  private static final int RANKING_LIMIT = 50;

  private final SseEmitterRegistry sseEmitterRegistry;
  private final RealtimeRankingQueryDao realtimeRankingQueryDao;

  public SseEmitter subscribeBreakoutSuccess() {
    return sseEmitterRegistry.create(BREAKOUT_SUCCESS_RANKING_KEY);
  }

  public SseEmitter subscribeBreakoutReady() {
    return sseEmitterRegistry.create(BREAKOUT_READY_RANKING_KEY);
  }

  public void updateRanking(StockRegime regime) {
    String key = createMessageKey(regime);
    if (key == null) {
      return;
    }

    List<RankedStockRow> ranked =
        realtimeRankingQueryDao.findRanked(regime.name(), LocalDate.now(), RANKING_LIMIT);
    if (ranked.isEmpty()) {
      return;
    }
    sseEmitterRegistry.broadcast(key, RANKING_EVENT, createItems(regime, ranked));
  }

  private String createMessageKey(StockRegime regime) {
    if (regime.equals(BREAKOUT_SUCCESS)) {
      return BREAKOUT_SUCCESS_RANKING_KEY;
    }
    if (regime.equals(BREAKOUT_READY)) {
      return BREAKOUT_READY_RANKING_KEY;
    }
    return null;
  }

  private Object createItems(StockRegime regime, List<RankedStockRow> ranked) {
    if (regime.equals(BREAKOUT_SUCCESS)) {
      return RealtimeBreakoutSuccessItem.from(ranked);
    }
    if (regime.equals(BREAKOUT_READY)) {
      return RealtimeBreakoutReadyItem.from(ranked);
    }
    return null;
  }
}
