package com.momentum.domain.pricepoint.service;

import com.momentum.domain.pricepoint.StockPricePointCalculationRepository;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePointCalculation;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.service.StockPricePointSlopeCalculator.SlopeResult;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SwingDoorCalculator {

  private static final BigDecimal SWING_DOOR_ERROR_PERCENT = BigDecimal.valueOf(3.0);

  private final StockCandleRepository stockCandleRepository;
  private final StockPricePointSlopeCalculator stockPricePointSlopeCalculator;
  private final StockPricePointRepository stockPricePointRepository;
  private final StockPricePointCalculationRepository pricePointCalculationRepository;

  public StockPricePoint resolve(StockPricePointCalculation lastCalculation, StockDailyCandle dailyCandle) {
    BigDecimal slopeUpperMax = lastCalculation.getSlopeUpperMax();
    BigDecimal slopeLowerMin = lastCalculation.getSlopeLowerMin();
    SlopeResult slope = stockPricePointSlopeCalculator.calculateSlope(
        lastCalculation.getStockPricePoint().getPrice(),
        lastCalculation.getStockPricePoint().getTradeDate(),
        dailyCandle.getClosePrice(),
        dailyCandle.getTradeDate(),
        SWING_DOOR_ERROR_PERCENT
    );
    slopeUpperMax = slopeUpperMax.max(slope.upper());
    slopeLowerMin = slopeLowerMin.min(slope.lower());

    if (slopeUpperMax.compareTo(slopeLowerMin) <= 0) {
      saveCalculation(lastCalculation.getStockPricePoint(), dailyCandle, slopeUpperMax, slopeLowerMin);
      return null;
    }
    StockDailyCandle lastCandle = stockCandleRepository.findLastCandleAfterDate(
            dailyCandle.getStock(),
            dailyCandle.getTradeDate())
        .orElseThrow(() -> new IllegalStateException("어제 캔들 없음, 추가바람"));
    return stockPricePointRepository.save(
        StockPricePoint.init(lastCandle.getClosePrice(), lastCandle.getVolume(),
            lastCandle.getTradeDate(), lastCandle.getStock())
    );
  }

  public void initializeCalculation(StockPricePoint anchorPoint, StockDailyCandle currentCandle) {
    SlopeResult slope = stockPricePointSlopeCalculator.calculateSlope(
        anchorPoint.getPrice(),
        anchorPoint.getTradeDate(),
        currentCandle.getClosePrice(),
        currentCandle.getTradeDate(),
        SWING_DOOR_ERROR_PERCENT
    );
    saveCalculation(anchorPoint, currentCandle, slope.upper(), slope.lower());
  }

  private void saveCalculation(StockPricePoint anchorPoint, StockDailyCandle currentCandle, BigDecimal slopeUpperMax,
      BigDecimal slopeLowerMin) {
    StockPricePointCalculation calculation = StockPricePointCalculation.create(currentCandle.getClosePrice(), slopeUpperMax,
        slopeLowerMin, anchorPoint);
    pricePointCalculationRepository.save(calculation);
  }
}
