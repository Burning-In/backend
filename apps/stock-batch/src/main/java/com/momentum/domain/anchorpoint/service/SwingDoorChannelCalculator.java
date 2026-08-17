package com.momentum.domain.anchorpoint.service;

import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointCalculation;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SwingDoorChannelCalculator {

  private static final BigDecimal SWING_DOOR_ERROR_PERCENT = BigDecimal.valueOf(3.0);

  private final StockAnchorPointSlopeCalculator stockAnchorPointSlopeCalculator;

  public TrendChannel narrowChannel(StockAnchorPointCalculation lastCalculation, StockDailyCandle dailyCandle) {
    if (lastCalculation == null || dailyCandle == null) {
      throw new IllegalArgumentException("lastCalculation과 dailyCandle은 null일 수 없다");
    }
    SlopeResult slope = calculateSlopeFrom(lastCalculation.getStockAnchorPoint(), dailyCandle);
    return new TrendChannel(
        lastCalculation.getSlopeUpperMax().max(slope.upper()),
        lastCalculation.getSlopeLowerMin().min(slope.lower())
    );
  }

  public TrendChannel openChannel(StockAnchorPoint anchorPoint, StockDailyCandle dailyCandle) {
    if (anchorPoint == null || dailyCandle == null) {
      throw new IllegalArgumentException("anchorPoint와 dailyCandle은 null일 수 없다");
    }
    SlopeResult slope = calculateSlopeFrom(anchorPoint, dailyCandle);
    return new TrendChannel(slope.upper(), slope.lower());
  }

  private SlopeResult calculateSlopeFrom(StockAnchorPoint anchorPoint, StockDailyCandle dailyCandle) {
    return stockAnchorPointSlopeCalculator.calculateSlope(
        anchorPoint.getPrice(),
        anchorPoint.getTradeDate(),
        dailyCandle.getClosePrice(),
        dailyCandle.getTradeDate(),
        SWING_DOOR_ERROR_PERCENT
    );
  }
}
