package com.momentum.domain.score;

import com.momentum.domain.stockcandle.StockDailyCandle;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Momentum {

  // 12개월 모멘텀 = (현재가 - 12개월전가) / 12개월전가
  private BigDecimal value;

  private Momentum(BigDecimal value) {
    this.value = value;
  }

  public static Momentum calculate(List<StockDailyCandle> candles) {
    long currentPrice = candles.get(0).getClosePrice();
    long pastPrice = candles.get(candles.size() - 1).getClosePrice();
    BigDecimal value = BigDecimal.valueOf(currentPrice)
        .subtract(BigDecimal.valueOf(pastPrice))
        .divide(BigDecimal.valueOf(pastPrice), 6, RoundingMode.HALF_UP);
    return new Momentum(value);
  }

  public static Momentum of(BigDecimal value) {
    return new Momentum(value);
  }
}
