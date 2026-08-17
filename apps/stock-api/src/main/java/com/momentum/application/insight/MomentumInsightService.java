package com.momentum.application.insight;

import com.momentum.infrastructure.query.InsightQueryDao;
import com.momentum.infrastructure.query.InsightRows.CandlePriceRow;
import com.momentum.infrastructure.query.InsightRows.RankScoreRow;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.MomentumResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MomentumInsightService {

  private static final int TRADING_DAYS_PER_YEAR = 252;
  private static final BigDecimal RATIO_TO_PERCENT = BigDecimal.valueOf(100);

  private final InsightQueryDao insightQueryDao;

  public MomentumResponse query(String stockCode, LocalDate at) {
    RankScoreRow row = insightQueryDao.findLatestRankScore(stockCode)
        .orElseThrow(() -> new NoSuchElementException("모멘텀 데이터가 없습니다: " + stockCode));

    BigDecimal yearlyPriceChangeRate = row.momentum()
        .multiply(RATIO_TO_PERCENT)
        .setScale(4, RoundingMode.HALF_UP);
    BigDecimal percentileRank = PercentileRank.of(row.momentum(), insightQueryDao.findMomentumScores(row.baseDate()));

    List<CandlePriceRow> closes = insightQueryDao.findRecentCloses(stockCode, at, TRADING_DAYS_PER_YEAR + 1);
    if (closes.isEmpty()) {
      throw new NoSuchElementException("거래일 데이터가 없습니다: " + stockCode);
    }
    CandlePriceRow currentClose = closes.getFirst();
    CandlePriceRow yearAgoClose = closes.getLast();

    return new MomentumResponse(
        yearAgoClose.closePrice(),
        yearAgoClose.tradeDate(),
        currentClose.closePrice(),
        currentClose.tradeDate(),
        yearlyPriceChangeRate,
        percentileRank
    );
  }
}
