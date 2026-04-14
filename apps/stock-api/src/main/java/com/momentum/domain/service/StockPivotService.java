package com.momentum.domain.service;

import com.momentum.application.dto.SlopeResult;
import com.momentum.domain.entity.StockDailyCandle;
import com.momentum.domain.entity.indicator.price.StockPivot;
import com.momentum.domain.entity.indicator.price.StockPivotCalculateHistory;
import com.momentum.domain.respository.StockCandleRepository;
import com.momentum.domain.respository.StockPivotCalculateHistoryRepository;
import com.momentum.domain.respository.StockPivotRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockPivotService {

  private static final BigDecimal PIVOT_ERROR_PERCENT = BigDecimal.valueOf(3.0);

  private final StockPivotCalculateHistoryRepository stockPivotCalculateHistoryRepository;
  private final StockCandleRepository stockCandleRepository;
  private final StockPivotRepository stockPivotRepository;
  private final StockPivotSlopCalculator stockPivotSlopCalculator;

  @Transactional
  public void resolvePivot(StockDailyCandle stockDailyCandle) {
    Optional<StockPivotCalculateHistory> calculationHistory = stockPivotCalculateHistoryRepository.findTopCalculationHistory(
        stockDailyCandle.getStock()
    );
    if (calculationHistory.isEmpty()) {
      Optional<StockPivot> lastPivot = stockPivotRepository.findTopByStockOrderByCreatedAtDesc(
          stockDailyCandle.getStock()
      );
      if (lastPivot.isEmpty()) {
        StockPivot high = StockPivot.create(
            stockDailyCandle.getClosePrice(),
            stockDailyCandle.getTradeDate(),
            stockDailyCandle.getStock()
        );
        stockPivotRepository.save(high);
      }
      if (lastPivot.isPresent()) {
        SlopeResult slope = stockPivotSlopCalculator.calculateSlope(
            lastPivot.get().getPrice(),
            lastPivot.get().getTradeDate(),
            stockDailyCandle.getClosePrice(),
            stockDailyCandle.getTradeDate(),
            PIVOT_ERROR_PERCENT
        );
        stockPivotCalculateHistoryRepository.save(
            StockPivotCalculateHistory.create(stockDailyCandle.getClosePrice(), slope.su(), slope.sl(), lastPivot.get())
        );
      }
      return;
    }

    BigDecimal suMax = calculationHistory.get().getSU_MAX();
    BigDecimal slMin = calculationHistory.get().getSL_MIN();
    SlopeResult slope = stockPivotSlopCalculator.calculateSlope(
        calculationHistory.get().getStockPivot().getPrice(),
        calculationHistory.get().getStockPivot().getTradeDate(),
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
      StockPivot savedPivot = stockPivotRepository.save(
          StockPivot.create(yesterdayCandle.getClosePrice(), yesterdayCandle.getTradeDate(), yesterdayCandle.getStock())
      );
      SlopeResult recalcSlope = stockPivotSlopCalculator.calculateSlope(
          yesterdayCandle.getClosePrice(),
          yesterdayCandle.getTradeDate(),
          stockDailyCandle.getClosePrice(),
          stockDailyCandle.getTradeDate(),
          PIVOT_ERROR_PERCENT
      );
      stockPivotCalculateHistoryRepository.save(
          StockPivotCalculateHistory.create(stockDailyCandle.getClosePrice(), recalcSlope.su(), recalcSlope.sl(), savedPivot)
      );
      return;
    }

    stockPivotCalculateHistoryRepository.save(
        StockPivotCalculateHistory.create(stockDailyCandle.getClosePrice(), suMax, slMin,
            calculationHistory.get().getStockPivot())
    );
  }
}
