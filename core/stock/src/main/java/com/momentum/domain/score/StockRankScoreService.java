package com.momentum.domain.score;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockRankScoreService {

  private static final int TRADING_DAYS_PER_YEAR = 252;

  private final StockCandleRepository stockCandleRepository;
  private final StockRankScoreRepository stockRankScoreRepository;

  public void calculateDailyRankScores(Stock stock, LocalDate baseDate) {
    // 252일치 캔들 조회
    List<StockDailyCandle> candles = stockCandleRepository
        .findRecentCandles(stock.getId(), baseDate, TRADING_DAYS_PER_YEAR);

    // 1년치 데이터 없으면 스킵
    if (candles.size() < TRADING_DAYS_PER_YEAR) {
      return;
    }

    BigDecimal momentum = calculateMomentum(candles);
    BigDecimal fip = calculateFip(candles, momentum);
    StockRankScore rankScore = StockRankScore.create(momentum, fip, baseDate, stock);

    stockRankScoreRepository.save(rankScore);
  }

  // 모멘텀 = (현재가 - 12개월전가) / 12개월전가
  private BigDecimal calculateMomentum(List<StockDailyCandle> candles) {
    if (candles == null || candles.size() < TRADING_DAYS_PER_YEAR) {
      throw new IllegalArgumentException();
    }
    // candles는 최신순 정렬 가정 /
    long currentPrice = candles.get(0).getClosePrice();
    long pastPrice = candles.get(candles.size() - 1).getClosePrice();

    BigDecimal current = BigDecimal.valueOf(currentPrice);
    BigDecimal past = BigDecimal.valueOf(pastPrice);

    return current.subtract(past).divide(past, 6, RoundingMode.HALF_UP);
  }

  // FIP = Sign(12개월 수익률) × [(하락일수 / 252) - (상승일수 / 252)]
  private BigDecimal calculateFip(List<StockDailyCandle> candles, BigDecimal momentum) {
    int positiveCount = 0;
    int negativeCount = 0;

    // candles는 최신순 정렬 가정
    // 전일 종가 대비 당일 종가로 상승/하락 판단
    for (int i = 0; i < candles.size() - 1; i++) {
      long currentClose = candles.get(i).getClosePrice();
      long previousClose = candles.get(i + 1).getClosePrice();
      long change = currentClose - previousClose;

      if (change > 0) {
        positiveCount++;
      }
      if (change < 0) {
        negativeCount++;
      }
    }

    BigDecimal pctPositive = BigDecimal.valueOf(positiveCount)
        .divide(BigDecimal.valueOf(TRADING_DAYS_PER_YEAR), 6, RoundingMode.HALF_UP);
    BigDecimal pctNegative = BigDecimal.valueOf(negativeCount)
        .divide(BigDecimal.valueOf(TRADING_DAYS_PER_YEAR), 6, RoundingMode.HALF_UP);

    return calculateSign(momentum).multiply(pctPositive.subtract(pctNegative));
  }

  private BigDecimal calculateSign(BigDecimal momentum) {
    if (momentum.compareTo(BigDecimal.ZERO) > 0) {
      return BigDecimal.ONE;
    }
    if (momentum.compareTo(BigDecimal.ZERO) < 0) {
      return BigDecimal.valueOf(-1);
    }
    return BigDecimal.ZERO;
  }
}
