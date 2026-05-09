package com.momentum.domain.score;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
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
    List<StockDailyCandle> candles = stockCandleRepository
        .findRecentCandles(stock.getId(), baseDate, TRADING_DAYS_PER_YEAR);

    if (candles.size() < TRADING_DAYS_PER_YEAR) {
      return;
    }

    StockRankScore rankScore = StockRankScore.create(candles, baseDate, stock);
    stockRankScoreRepository.save(rankScore);
  }
}
