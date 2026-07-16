package com.momentum.domain.stock;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_FAILED;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.domain.stock.StockRegime.DIRECTION_UNDETERMINED;
import static com.momentum.domain.stock.StockRegime.DOWNSIDE_BREAK;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import org.springframework.stereotype.Component;

@Component
public class DailyRegimePolicy {

  public StockRegime decide(Stock stock, long closePrice, StockPricePoint recentPricePoint,
      StockBase currentBase, double breakoutThreshold, double lineApproachThreshold) {
    long resistancePrice = currentBase.getHighestResistanceLine().getPrice();
    long supportPrice = currentBase.getLowestSupportLine().getPrice();
    long recentPointPrice = recentPricePoint.getPrice();

    if (isDownsideBreak(closePrice, supportPrice, recentPointPrice)) {
      return DOWNSIDE_BREAK;
    }
    if (isBreakoutFailed(closePrice, resistancePrice, recentPointPrice)) {
      return BREAKOUT_FAILED;
    }
    if (isBreakoutSuccess(stock, closePrice, resistancePrice, recentPointPrice, breakoutThreshold)) {
      return BREAKOUT_SUCCESS;
    }
    if (isBreakoutReady(stock, closePrice, resistancePrice, recentPointPrice, currentBase,
        lineApproachThreshold)) {
      return BREAKOUT_READY;
    }
    return DIRECTION_UNDETERMINED;
  }

  private boolean isDownsideBreak(long closePrice, long supportPrice, long recentPointPrice) {
    return closePrice < supportPrice && closePrice < recentPointPrice;
  }

  private boolean isBreakoutFailed(long closePrice, long resistancePrice, long recentPointPrice) {
    return closePrice < resistancePrice && closePrice < recentPointPrice;
  }

  private boolean isBreakoutSuccess(Stock stock, long closePrice, long resistancePrice,
      long recentPointPrice, double breakoutThreshold) {
    return stock.getStockTrend().equals(StockTrend.UPTREND)
        && calculateGap(resistancePrice, closePrice) > breakoutThreshold
        && closePrice > recentPointPrice;
  }

  private boolean isBreakoutReady(Stock stock, long closePrice, long resistancePrice,
      long recentPointPrice, StockBase currentBase, double lineApproachThreshold) {
    return stock.getStockTrend().equals(StockTrend.UPTREND)
        && (currentBase.getVcp().isVcp()
        || (calculateGap(resistancePrice, closePrice) >= -lineApproachThreshold
        && closePrice > recentPointPrice));
  }

  private double calculateGap(long linePrice, long currentPrice) {
    if (linePrice == 0) {
      return 0.0;
    }
    return ((double) (currentPrice - linePrice) / linePrice) * 100;
  }
}
