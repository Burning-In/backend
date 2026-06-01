package com.momentum.domain.stock;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;

public enum StockRegime {
  BREAKOUT_SUCCESS,
  BREAKOUT_READY,
  BREAKOUT_FAILED,
  DOWNSIDE_BREAK,
  DIRECTION_UNDETERMINED, // 방향 미정 (확정된 상태)
  UNKNOWN; // 실시간 판단 보류 — 기존 레짐 유지

  public static StockRegime decideRealTimeStockRegime(long currentPrice, StockBase currentStockBase, Stock stock,
      double breakOutThreshold, long lastPricePointPrice) {
    // # 하방이탈 : 현재가 < 지지선
    if (currentStockBase.getLowestSupportLine() != null
        && currentStockBase.getLowestSupportLine().getPrice() > currentPrice) {
      return DOWNSIDE_BREAK;
    }

    // # 돌파실패
    if (stock.getStockRegime().equals(BREAKOUT_SUCCESS) && lastPricePointPrice > currentPrice) {
      return BREAKOUT_FAILED;
    }

    // # 돌파성공 & 돌파실패
    long resistanceUpperBound = currentStockBase.getHighestResistanceLine()
        .getUpperBound(breakOutThreshold);
    if (currentStockBase.isVcp()) {
      if (currentPrice > resistanceUpperBound) {
        return BREAKOUT_SUCCESS;
      }
      return BREAKOUT_READY;
    }
    return UNKNOWN;
  }

  public static StockRegime determineDailyRegime(Stock stock, long closePrice, StockPricePoint recentPricePoint,
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
      return BREAKOUT_SUCCESS;
    }
    if (isBreakoutReady(stock, closePrice, resistancePrice, recentPointPrice, currentBase, lineApproachThreshold)) {
      return BREAKOUT_READY;
    }
    return DIRECTION_UNDETERMINED;
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
