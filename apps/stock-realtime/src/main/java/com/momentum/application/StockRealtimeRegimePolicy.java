package com.momentum.application;

import static com.momentum.sharedkernel.StockRegime.BREAKOUT_FAILED;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_READY;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.sharedkernel.StockRegime.DOWNSIDE_BREAK;
import static com.momentum.sharedkernel.StockRegime.UNKNOWN;

import com.momentum.sharedkernel.StockRegime;
import com.momentum.sharedkernel.StockTrend;
import com.momentum.infrastructure.query.RealtimeRegimeRow;
import org.springframework.stereotype.Component;

@Component
public class StockRealtimeRegimePolicy {

  public StockRegime decide(long currentPrice, RealtimeRegimeRow source, double thresholdPercent) {
    validate(source);

    StockRegime currentRegime = StockRegime.valueOf(source.stockRegime());
    boolean uptrend = StockTrend.valueOf(source.stockTrend()) == StockTrend.UPTREND;
    long resistanceUpperBound = upperBound(source.resistancePrice(), thresholdPercent);

    if (lowerBound(source.supportPrice(), thresholdPercent) > currentPrice) {
      return DOWNSIDE_BREAK;
    }
    if (currentRegime == BREAKOUT_SUCCESS && source.lastAnchorPointPrice() > currentPrice) {
      return BREAKOUT_FAILED;
    }
    if (uptrend && source.vcp() && currentPrice > resistanceUpperBound) {
      return BREAKOUT_SUCCESS;
    }
    if (uptrend && currentPrice > resistanceUpperBound) {
      return BREAKOUT_READY;
    }

    return UNKNOWN;
  }

  private void validate(RealtimeRegimeRow source) {
    if (source == null) {
      throw new IllegalArgumentException("source is null");
    }
    if (source.hasNoBase()) {
      throw new IllegalArgumentException("currentBase resistance/support line is null");
    }
    if (source.hasNoAnchorPoint()) {
      throw new IllegalArgumentException("lastAnchorPoint is null");
    }
  }

  private long upperBound(long price, double thresholdPercent) {
    return (long) (price * (thresholdPercent / 100.0 + 1));
  }

  private long lowerBound(long price, double thresholdPercent) {
    return (long) (price * (-thresholdPercent / 100.0 + 1));
  }
}
