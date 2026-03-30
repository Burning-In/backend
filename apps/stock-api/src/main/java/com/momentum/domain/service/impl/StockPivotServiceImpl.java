package com.momentum.domain.service.impl;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.entity.indicator.StockPivotType;
import com.momentum.domain.respository.StockCandleRepository;
import com.momentum.domain.respository.StockRepository;
import com.momentum.domain.service.StockPivotService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockPivotServiceImpl implements StockPivotService {

  private static final double PIVOT_THRESHOLD_PERCENT = 4.0;

  private final StockCandleRepository stockCandleRepository;
  private final StockRepository stockRepository;

  @Override
  @Transactional
  public StockCandle determineDailyPivot(String stockCode, LocalDate tradeDate) {
    Stock stock = stockRepository.findByStockCode(stockCode)
        .orElseThrow(() -> new IllegalArgumentException("stock not found"));

    StockCandle twoDaysAgoCandle = stockCandleRepository.findDailyCandle(stock.getId(), tradeDate.minusDays(2))
        .orElseThrow(IllegalArgumentException::new);
    StockCandle oneDayAgoCandle = stockCandleRepository.findDailyCandle(stock.getId(), tradeDate.minusDays(1))
        .orElseThrow(IllegalArgumentException::new);
    StockCandle currentCandle = stockCandleRepository.findDailyCandle(stock.getId(), tradeDate)
        .orElseThrow(IllegalArgumentException::new);

    double changeFromTwoToOne = getChangePercent(
        twoDaysAgoCandle.getClosePrice(),
        oneDayAgoCandle.getClosePrice()
    );
    double changeFromTwoToCurrent = getChangePercent(
        twoDaysAgoCandle.getClosePrice(),
        currentCandle.getClosePrice()
    );

    // Pivot Low (-++)
    if (twoDaysAgoCandle.getStockPriceTrend().isLower()
        && (changeFromTwoToOne >= PIVOT_THRESHOLD_PERCENT
        || changeFromTwoToCurrent >= PIVOT_THRESHOLD_PERCENT)) {

      twoDaysAgoCandle.updatePivotType(StockPivotType.PIVOT_LOW);
      return stockCandleRepository.save(twoDaysAgoCandle);
    }

    // Pivot High (+--)
    if (twoDaysAgoCandle.getStockPriceTrend().isUpper()
        && (changeFromTwoToOne <= -PIVOT_THRESHOLD_PERCENT
        || changeFromTwoToCurrent <= -PIVOT_THRESHOLD_PERCENT)) {

      twoDaysAgoCandle.updatePivotType(StockPivotType.PIVOT_HIGH);
      return stockCandleRepository.save(twoDaysAgoCandle);
    }

    twoDaysAgoCandle.updatePivotType(StockPivotType.FLAT);
    return stockCandleRepository.save(twoDaysAgoCandle);
  }

  private double getChangePercent(Long previousPrice, Long currentPrice) {
    if (previousPrice == null || currentPrice == null) {
      throw new IllegalArgumentException("price must not be null");
    }

    if (previousPrice == 0L) {
      return 0.0; // division by zero 방지
    }

    return ((double) (currentPrice - previousPrice) / previousPrice) * 100;
  }
}
