package com.momentum.application;

import com.momentum.domain.score.StockRankScore;
import com.momentum.domain.score.StockRankScoreRepository;
import com.momentum.domain.stock.StockCode;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stocktick.StockTick;
import com.momentum.domain.stocktick.StockTickRepository;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutReadyResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutReadyResponse.BreakoutReadyItem;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutSuccessResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutSuccessResponse.BreakoutSuccessItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingService {

  private static final int RANKING_LIMIT = 50;

  private final StockRankScoreRepository stockRankScoreRepository;
  private final StockTickRepository stockTickRepository;

  @Transactional(readOnly = true)
  public BreakoutSuccessResponse getBreakoutSuccessRanking(LocalDateTime at) {
    List<BreakoutSuccessItem> stocks = findRanked(StockRegime.BREAKOUT_SUCCESS, at).stream()
        .map(score -> new BreakoutSuccessItem(
            score.getStock().getName(),
            score.getStock().getCode(),
            getLastPrice(score.getStock().getCode(), at),
            score.getMomentumScore().getValue(),
            score.getFrogInPanScore().getValue()))
        .toList();
    return new BreakoutSuccessResponse(stocks);
  }

  @Transactional(readOnly = true)
  public BreakoutReadyResponse getBreakoutReadyRanking(LocalDateTime at) {
    List<BreakoutReadyItem> stocks = findRanked(StockRegime.BREAKOUT_READY, at).stream()
        .map(score -> new BreakoutReadyItem(
            score.getStock().getName(),
            score.getStock().getCode(),
            getLastPrice(score.getStock().getCode(), at),
            score.getMomentumScore().getValue(),
            score.getFrogInPanScore().getValue()))
        .toList();
    return new BreakoutReadyResponse(stocks);
  }

  private List<StockRankScore> findRanked(StockRegime regime, LocalDateTime at) {
    return stockRankScoreRepository.findLastStockRankScore(regime, at.toLocalDate(), RANKING_LIMIT);
  }

  private BigDecimal getLastPrice(String stockCode, LocalDateTime at) {
    return stockTickRepository.findLatestTick(StockCode.getCode(stockCode), at)
        .map(StockTick::getPrice)
        .map(BigDecimal::valueOf)
        .orElse(null);
  }
}
