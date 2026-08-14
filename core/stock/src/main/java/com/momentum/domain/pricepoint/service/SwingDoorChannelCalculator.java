package com.momentum.domain.pricepoint.service;

import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointCalculation;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SwingDoorChannelCalculator {

  private static final BigDecimal SWING_DOOR_ERROR_PERCENT = BigDecimal.valueOf(3.0);

  private final StockPricePointSlopeCalculator stockPricePointSlopeCalculator;

  public TrendChannel narrowChannel(StockPricePointCalculation lastCalculation, StockDailyCandle dailyCandle) {
    if (lastCalculation == null || dailyCandle == null) {
      throw new IllegalArgumentException("lastCalculation과 dailyCandle은 null일 수 없다");
    }
    SlopeResult slope = calculateSlopeFrom(lastCalculation.getStockPricePoint(), dailyCandle);
    return new TrendChannel(
        lastCalculation.getSlopeUpperMax().max(slope.upper()),
        lastCalculation.getSlopeLowerMin().min(slope.lower())
    );
  }

  public TrendChannel openChannel(StockPricePoint anchorPoint, StockDailyCandle dailyCandle) {
    if (anchorPoint == null || dailyCandle == null) {
      throw new IllegalArgumentException("anchorPoint와 dailyCandle은 null일 수 없다");
    }
    SlopeResult slope = calculateSlopeFrom(anchorPoint, dailyCandle);
    return new TrendChannel(slope.upper(), slope.lower());
  }

  private SlopeResult calculateSlopeFrom(StockPricePoint anchorPoint, StockDailyCandle dailyCandle) {
    return stockPricePointSlopeCalculator.calculateSlope(
        anchorPoint.getPrice(),
        anchorPoint.getTradeDate(),
        dailyCandle.getClosePrice(),
        dailyCandle.getTradeDate(),
        SWING_DOOR_ERROR_PERCENT
    );
  }
}
