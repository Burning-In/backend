package com.momentum.application;

import com.momentum.sharedkernel.StockRegime;
import com.momentum.infrastructure.query.RealtimeRegimeQueryDao;
import com.momentum.infrastructure.query.RealtimeRegimeRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockRealtimeRegimeService {

  private static final double THRESHOLD_PERCENT = 5.0;

  private final RealtimeRegimeQueryDao realtimeRegimeQueryDao;
  private final StockRealtimeRegimePolicy stockRealtimeRegimePolicy;
  private final RealtimeRankingFacade realtimeRankingFacade;

  public void resolveRealtimeRegime(String stockCode, long currentPrice) {
    RealtimeRegimeRow source = realtimeRegimeQueryDao.findRegimeSource(stockCode)
        .orElseThrow(IllegalArgumentException::new);
    if (source.hasNoBase() || source.hasNoAnchorPoint()) {
      throw new IllegalStateException();
    }

    StockRegime currentRegime = StockRegime.valueOf(source.stockRegime());
    StockRegime newRegime = stockRealtimeRegimePolicy.decide(currentPrice, source, THRESHOLD_PERCENT);
    if (newRegime == StockRegime.UNKNOWN || newRegime == currentRegime) {
      return;
    }

    boolean updated = realtimeRegimeQueryDao.updateRegime(stockCode, currentRegime.name(), newRegime.name());
    if (!updated) {
      return;
    }
    realtimeRankingFacade.updateRanking(currentRegime);
    realtimeRankingFacade.updateRanking(newRegime);
  }
}
