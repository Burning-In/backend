package com.momentum.domain.pricepoint;

import com.momentum.domain.pricepoint.StockPricePointSlopCalculator.SlopeResult;
import com.momentum.domain.pricepoint.entity.StockPivotCalculation;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
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

  private final StockPricePointCalculationRepository pricePointCalculationRepository;
  private final StockCandleRepository stockCandleRepository;
  private final StockPricePointRepository stockPricePointRepository;
  private final StockPricePointSlopCalculator stockPricePointSlopCalculator;

  @Transactional
  public void resolvePricePoint(StockDailyCandle stockDailyCandle) {
    Optional<StockPivotCalculation> calculationHistory = pricePointCalculationRepository.findTopCalculationHistory(
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
        pricePointCalculationRepository.save(
            StockPivotCalculation.create(stockDailyCandle.getClosePrice(), slope.su(), slope.sl(), lastPricePoint.get())
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
      pricePointCalculationRepository.save(
          StockPivotCalculation.create(stockDailyCandle.getClosePrice(), recalcSlope.su(), recalcSlope.sl(), savedPivot)
      );
      return;
    }

    pricePointCalculationRepository.save(
        StockPivotCalculation.create(stockDailyCandle.getClosePrice(), suMax, slMin,
            calculationHistory.get().getStockPricePoint())
    );
  }
}
