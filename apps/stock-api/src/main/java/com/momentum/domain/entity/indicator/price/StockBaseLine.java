package com.momentum.domain.entity.indicator.price;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockBaseLine extends BaseEntity {

  private Long price;

  @Enumerated
  private StockLineStrength stockLineStrength;

  @Enumerated(EnumType.STRING)
  private StockLineType lineType;

  @ManyToOne
  private StockBase stockBase;

  private StockBaseLine(Long price, StockLineStrength stockLineStrength,
      StockLineType lineType, StockBase stockBase) {
    this.price = price;
    this.stockLineStrength = stockLineStrength;
    this.lineType = lineType;
    this.stockBase = stockBase;
  }

  public static StockBaseLine resistance(long closePrice, Long currentVolume, Long averageDailyVolume, StockBase stockBase) {
    return new StockBaseLine(
        closePrice,
        StockLineStrength.create(currentVolume, averageDailyVolume),
        StockLineType.RESISTANCE,
        stockBase
    );
  }

  public static StockBaseLine support(long closePrice, Long currentVolume, Long averageDailyVolume, StockBase stockBase) {
    return new StockBaseLine(
        closePrice,
        StockLineStrength.create(currentVolume, averageDailyVolume),
        StockLineType.SUPPORT,
        stockBase
    );
  }

  public void updateStrength(Long additionalVolume, Long averageDailyVolume) {
    this.stockLineStrength.touch(additionalVolume, averageDailyVolume);
  }
}
