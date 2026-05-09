package com.momentum.application.insight;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.StockRegimeResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegimeInsightService {

  private final StockBaseRepository stockBaseRepository;
  private final StockCandleRepository stockCandleRepository;

  public StockRegimeResponse query(Stock stock, LocalDate at) {
    StockDailyCandle candle = stockCandleRepository.findRecentCandle(stock, at)
        .orElseThrow();
    long currentPrice = candle.getClosePrice();

    Optional<StockBase> baseOpt = stockBaseRepository.findCurrentBaseWithLines(stock);
    if (baseOpt.isEmpty() || stock.getStockRegime() == StockRegime.UNDETERMINED) {
      return new StockRegimeResponse(StockRegime.UNDETERMINED, currentPrice, null, null, null);
    }

    StockBase base = baseOpt.get();
    long resistance = base.getHighestResistanceLine().getPrice();
    long support = base.getLowestSupportLine().getPrice();
    StockRegime regime = stock.getStockRegime();

    long referenceLine;
    if (regime == StockRegime.DOWNSIDE_BREAK) {
      referenceLine = support;
    } else {
      referenceLine = resistance;
    }
    BigDecimal changeRate = changeRate(referenceLine, currentPrice);

    return new StockRegimeResponse(regime, currentPrice, support, resistance, changeRate);
  }

  private BigDecimal changeRate(long base, long current) {
    if (base == 0) return BigDecimal.ZERO;
    return BigDecimal.valueOf((double) (current - base) / base * 100)
        .setScale(4, RoundingMode.HALF_UP);
  }
}
