package com.momentum.application;

import com.momentum.sharedkernel.StockRegime;
import com.momentum.infrastructure.query.RankedStockRow;
import com.momentum.infrastructure.query.RankingQueryDao;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutReadyResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutReadyResponse.BreakoutReadyItem;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutSuccessResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutSuccessResponse.BreakoutSuccessItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingService {

  private static final int RANKING_LIMIT = 50;
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private final RankingQueryDao rankingQueryDao;

  public BreakoutSuccessResponse getBreakoutSuccessRanking(LocalDateTime at) {
    List<RankedStockRow> ranked = findRanked(StockRegime.BREAKOUT_SUCCESS, at);
    Map<String, BigDecimal> prices = findLatestPrices(ranked, at);

    List<BreakoutSuccessItem> stocks = ranked.stream()
        .map(row -> new BreakoutSuccessItem(
            row.stockName(),
            row.stockCode(),
            prices.get(row.stockCode()),
            row.momentum(),
            row.fip()))
        .toList();
    return new BreakoutSuccessResponse(stocks);
  }

  public BreakoutReadyResponse getBreakoutReadyRanking(LocalDateTime at) {
    List<RankedStockRow> ranked = findRanked(StockRegime.BREAKOUT_READY, at);
    Map<String, BigDecimal> prices = findLatestPrices(ranked, at);

    List<BreakoutReadyItem> stocks = ranked.stream()
        .map(row -> new BreakoutReadyItem(
            row.stockName(),
            row.stockCode(),
            prices.get(row.stockCode()),
            row.momentum(),
            row.fip()))
        .toList();
    return new BreakoutReadyResponse(stocks);
  }

  private List<RankedStockRow> findRanked(StockRegime regime, LocalDateTime at) {
    return rankingQueryDao.findRanked(regime.name(), at.toLocalDate(), RANKING_LIMIT);
  }

  private Map<String, BigDecimal> findLatestPrices(List<RankedStockRow> ranked, LocalDateTime at) {
    List<String> stockCodes = ranked.stream()
        .map(RankedStockRow::stockCode)
        .toList();
    return rankingQueryDao.findLatestPricesByStockCode(stockCodes, at.atZone(KST));
  }
}
