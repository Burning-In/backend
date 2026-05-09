package com.momentum.application.insight;

import com.momentum.domain.score.StockRankScore;
import com.momentum.domain.score.StockRankScoreRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.MomentumResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MomentumInsightService {

  private static final int TRADING_DAYS_PER_YEAR = 252;

  private final StockCandleRepository stockCandleRepository;
  private final StockRankScoreRepository stockRankScoreRepository;

  public MomentumResponse query(Stock stock, LocalDate at) {
    StockRankScore rankScore = stockRankScoreRepository.findLatestByStock(stock)
        .orElseThrow(() -> new NoSuchElementException("모멘텀 데이터가 없습니다: " + stock.getCode()));

    // momentum은 소수 비율(0.20 = 20%)로 저장됨
    BigDecimal yearlyPriceChangeRate = rankScore.getMomentum().getValue()
        .multiply(BigDecimal.valueOf(100))
        .setScale(4, RoundingMode.HALF_UP);

    BigDecimal percentileRank = computePercentile(rankScore);

    // 가격/날짜 정보는 캔들에서 조회
    StockDailyCandle currentCandle = stockCandleRepository.findRecentCandle(stock, at)
        .orElseThrow();
    List<StockDailyCandle> candles = stockCandleRepository.findRecentCandles(
        stock.getId(), at, TRADING_DAYS_PER_YEAR + 1);
    StockDailyCandle yearAgoCandle = candles.get(candles.size() - 1);

    return new MomentumResponse(
        yearAgoCandle.getClosePrice(),
        yearAgoCandle.getTradeDate(),
        currentCandle.getClosePrice(),
        currentCandle.getTradeDate(),
        yearlyPriceChangeRate,
        percentileRank
    );
  }

  private BigDecimal computePercentile(StockRankScore myScore) {
    List<StockRankScore> allScores = stockRankScoreRepository.findAllByBaseDate(myScore.getBaseDate());
    List<BigDecimal> nonNull = allScores.stream()
        .map(s -> s.getMomentum().getValue())
        .filter(Objects::nonNull)
        .toList();
    if (nonNull.isEmpty()) {
      return null;
    }
    long below = nonNull.stream().filter(v -> v.compareTo(myScore.getMomentum().getValue()) < 0).count();
    return BigDecimal.valueOf((double) below / nonNull.size() * 100).setScale(1, RoundingMode.HALF_UP);
  }
}
