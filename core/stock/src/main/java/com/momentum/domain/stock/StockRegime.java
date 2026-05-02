package com.momentum.domain.stock;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;

public enum StockRegime {
  DOWNSIDE_BREAK,
  BREAKOUT_FAILED,
  BREAKOUT_START,
  BREAKOUT_READY,
  UNDETERMINED;

  public static StockRegime determineRegime(Stock stock, long closePrice, StockPricePoint recentPricePoint,
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
    if (isBreakoutStart(stock, closePrice, resistancePrice, recentPointPrice, breakoutThreshold)) {
      return BREAKOUT_START;
    }
    if (isBreakoutReady(stock, closePrice, resistancePrice, recentPointPrice, currentBase, lineApproachThreshold)) {
      return BREAKOUT_READY;
    }
    return UNDETERMINED;
  }

  private static boolean isDownsideBreak(long closePrice, long supportPrice, long recentPointPrice) {
    return closePrice < supportPrice && closePrice < recentPointPrice;
  }

  private static boolean isBreakoutFailed(long closePrice, long resistancePrice, long recentPointPrice) {
    return closePrice < resistancePrice && closePrice < recentPointPrice;
  }

  private static boolean isBreakoutStart(Stock stock, long closePrice, long resistancePrice,
      long recentPointPrice, double breakoutThreshold) {
    return stock.getStockTrend().equals(StockTrend.UPTREND)
        && calculateGap(resistancePrice, closePrice) > breakoutThreshold
        && closePrice > recentPointPrice;
  }

  private static boolean isBreakoutReady(Stock stock, long closePrice, long resistancePrice,
      long recentPointPrice, StockBase currentBase, double lineApproachThreshold) {
    return stock.getStockTrend().equals(StockTrend.UPTREND)
        && (currentBase.getVcp().isVcp()
        || (calculateGap(resistancePrice, closePrice) >= -lineApproachThreshold
        && closePrice > recentPointPrice));
  }

  private static double calculateGap(long linePrice, long currentPrice) {
    if (linePrice == 0) {
      return 0.0;
    }
    return ((double) (currentPrice - linePrice) / linePrice) * 100;
  }
}
