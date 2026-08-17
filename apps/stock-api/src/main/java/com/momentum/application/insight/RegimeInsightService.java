package com.momentum.application.insight;

import com.momentum.sharedkernel.StockRegime;
import com.momentum.infrastructure.query.InsightQueryDao;
import com.momentum.infrastructure.query.InsightRows.RegimeRow;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.StockRegimeResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegimeInsightService {

  private final InsightQueryDao insightQueryDao;

  public StockRegimeResponse query(String stockCode, LocalDate at) {
    RegimeRow row = insightQueryDao.findRegime(stockCode, at)
        .orElseThrow(() -> new NoSuchElementException("종목을 찾을 수 없습니다: " + stockCode));

    long currentPrice = row.currentPrice();
    StockRegime regime = StockRegime.valueOf(row.stockRegime());
    if (hasNoBase(row) || regime == StockRegime.UNKNOWN) {
      return new StockRegimeResponse(StockRegime.UNKNOWN, currentPrice, null, null, null);
    }

    long support = row.supportPrice();
    long resistance = row.resistancePrice();
    BigDecimal changeRate = changeRate(referenceLineOf(regime, support, resistance), currentPrice);

    return new StockRegimeResponse(regime, currentPrice, support, resistance, changeRate);
  }

  private boolean hasNoBase(RegimeRow row) {
    return row.supportPrice() == null || row.resistancePrice() == null;
  }

  private long referenceLineOf(StockRegime regime, long support, long resistance) {
    if (regime == StockRegime.DOWNSIDE_BREAK) {
      return support;
    }
    return resistance;
  }

  private BigDecimal changeRate(long base, long current) {
    if (base == 0) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf((double) (current - base) / base * 100)
        .setScale(4, RoundingMode.HALF_UP);
  }
}
