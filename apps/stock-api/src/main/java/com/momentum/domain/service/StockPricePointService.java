package com.momentum.domain.service;

import com.momentum.application.dto.SlopeResult;
import com.momentum.domain.entity.StockDailyCandle;
import com.momentum.domain.entity.indicator.price.StockPricePoint;
import com.momentum.domain.entity.indicator.price.StockPivotCalculateHistory;
import com.momentum.domain.respository.StockCandleRepository;
import com.momentum.domain.respository.StockPivotCalculateHistoryRepository;
import com.momentum.domain.respository.StockPricePointRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockPricePointService {

  private static final BigDecimal PIVOT_ERROR_PERCENT = BigDecimal.valueOf(3.0);

  private final StockPivotCalculateHistoryRepository stockPricePointCalculateHistoryRepository;
  private final StockCandleRepository stockCandleRepository;
  private final StockPricePointRepository stockPricePointRepository;
  private final StockPricePointSlopCalculator stockPricePointSlopCalculator;

  @Transactional
  public void resolvePricePoint(StockDailyCandle stockDailyCandle) {
    Optional<StockPivotCalculateHistory> calculationHistory = stockPricePointCalculateHistoryRepository.findTopCalculationHistory(
        stockDailyCandle.getStock()
    );
    if (calculationHistory.isEmpty()) {
      Optional<StockPricePoint> lastPricePoint = stockPricePointRepository.findTopByStockOrderByCreatedAtDesc(
          stockDailyCandle.getStock()
      );
      if (lastPricePoint.isEmpty()) {
        StockPricePoint high = StockPricePoint.create(
            stockDailyCandle.getClosePrice(),
            stockDailyCandle.getVolume(),
            stockDailyCandle.getTradeDate(),
            stockDailyCandle.getStock()
        );
        stockPricePointRepository.save(high);
      }
      if (lastPricePoint.isPresent()) {
        SlopeResult slope = stockPricePointSlopCalculator.calculateSlope(
            lastPricePoint.get().getPrice(),
            lastPricePoint.get().getTradeDate(),
            stockDailyCandle.getClosePrice(),
            stockDailyCandle.getTradeDate(),
            PIVOT_ERROR_PERCENT
        );
        stockPricePointCalculateHistoryRepository.save(
            StockPivotCalculateHistory.create(stockDailyCandle.getClosePrice(), slope.su(), slope.sl(), lastPricePoint.get())
        );
      }
      return;
    }

    BigDecimal suMax = calculationHistory.get().getSU_MAX();
    BigDecimal slMin = calculationHistory.get().getSL_MIN();
    SlopeResult slope = stockPricePointSlopCalculator.calculateSlope(
        calculationHistory.get().getStockPricePoint().getPrice(),
        calculationHistory.get().getStockPricePoint().getTradeDate(),
        stockDailyCandle.getClosePrice(),
        stockDailyCandle.getTradeDate(),
        PIVOT_ERROR_PERCENT
    );

    suMax = suMax.max(slope.su());
    slMin = slMin.min(slope.sl());

    if (suMax.compareTo(slMin) > 0) {
      LocalDate yesterday = stockDailyCandle.getTradeDate().minusDays(1);
      StockDailyCandle yesterdayCandle = stockCandleRepository.findByStockAndDate(stockDailyCandle.getStock(), yesterday)
          .orElseThrow(() -> new IllegalStateException("어제 캔들 없음"));
      StockPricePoint savedPivot = stockPricePointRepository.save(
          StockPricePoint.create(yesterdayCandle.getClosePrice(), yesterdayCandle.getVolume(), yesterdayCandle.getTradeDate(),
              yesterdayCandle.getStock())
      );
      SlopeResult recalcSlope = stockPricePointSlopCalculator.calculateSlope(
          yesterdayCandle.getClosePrice(),
          yesterdayCandle.getTradeDate(),
          stockDailyCandle.getClosePrice(),
          stockDailyCandle.getTradeDate(),
          PIVOT_ERROR_PERCENT
      );
      stockPricePointCalculateHistoryRepository.save(
          StockPivotCalculateHistory.create(stockDailyCandle.getClosePrice(), recalcSlope.su(), recalcSlope.sl(), savedPivot)
      );
      return;
    }

    stockPricePointCalculateHistoryRepository.save(
        StockPivotCalculateHistory.create(stockDailyCandle.getClosePrice(), suMax, slMin,
            calculationHistory.get().getStockPricePoint())
    );
  }
}
