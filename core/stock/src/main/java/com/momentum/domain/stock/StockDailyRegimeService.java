package com.momentum.domain.stock;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockDailyRegimeService {

  private static final double BREAKOUT_THRESHOLD = 3.0;
  private static final double APPROACH_THRESHOLD = 3.0;

  private final StockRepository stockRepository;
  private final StockBaseRepository stockBaseRepository;
  private final StockPricePointRepository stockPricePointRepository;

  public void finalizeDailyState(StockDailyCandle stockDailyCandle) {
    Stock stock = stockDailyCandle.getStock();
    long closePrice = stockDailyCandle.getClosePrice();

    Optional<StockBase> currentBaseOpt = stockBaseRepository.findCurrentBaseWithLines(stock);
    if (currentBaseOpt.isEmpty()) {
      stock.update(StockRegime.UNDETERMINED);
      stockRepository.save(stock);
      return;
    }

    StockBase currentBase = currentBaseOpt.get();

    Optional<StockPricePoint> recentPricePointOpt = stockPricePointRepository.findLatestByStock(stock);
    if (recentPricePointOpt.isEmpty()) {
      stock.update(StockRegime.UNDETERMINED);
      stockRepository.save(stock);
      return;
    }

    long recentPivotPrice = recentPricePointOpt.get().getPrice();
    long resistancePrice = currentBase.getHighestResistancePrice();
    long supportPrice = currentBase.getLowestSupportLinePrice();

    StockRegime regime = determineRegime(stock, closePrice, recentPivotPrice, resistancePrice, supportPrice, currentBase);
    stock.update(regime);
    stockRepository.save(stock);
  }

  private StockRegime determineRegime(Stock stock, long closePrice, long recentPivotPrice,
      long resistancePrice, long supportPrice, StockBase currentBase) {

    // # 하방이탈: (종가 < 지지선) AND (종가 < PP)
    if (closePrice < supportPrice && closePrice < recentPivotPrice) {
      return StockRegime.DOWNSIDE_BREAK;
    }

    // # 돌파실패: (종가 < 저항선) AND (종가 < PP)
    if (closePrice < resistancePrice && closePrice < recentPivotPrice) {
      return StockRegime.BREAKOUT_FAILED;
    }

    // # 돌파시작: (상승 템플릿) AND (종가 > 저항선 +3%) AND (종가 > PP)
    if (stock.getStockTrend().equals(StockTrend.UPTREND)
        && calculateGap(resistancePrice, closePrice) > BREAKOUT_THRESHOLD
        && closePrice > recentPivotPrice) {
      return StockRegime.BREAKOUT_START;
    }

    // # 돌파준비: (상승 템플릿) AND (VCP OR (저항선 -3% 이상 AND 종가 > PP))
    if (stock.getStockTrend().equals(StockTrend.UPTREND)) {
      boolean isVcp = currentBase.isVcp();
      boolean isApproaching = calculateGap(resistancePrice, closePrice) >= -APPROACH_THRESHOLD
          && closePrice > recentPivotPrice;
      if (isVcp || isApproaching) {
        return StockRegime.BREAKOUT_READY;
      }
    }

    // # 방향미정: 위 4개 케이스 미해당
    return StockRegime.UNDETERMINED;
  }

  private double calculateGap(long linePrice, long currentPrice) {
    if (linePrice == 0) {
      return 0.0;
    }
    return ((double) (currentPrice - linePrice) / linePrice) * 100;
  }
}
