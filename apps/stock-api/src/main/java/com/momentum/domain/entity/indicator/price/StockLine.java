package com.momentum.domain.entity.indicator.price;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockLine extends BaseEntity {

  private Long price;

  @Enumerated
  private StockLineStrength stockLineStrength;

  @Enumerated(EnumType.STRING)
  private StockLineType lineType;

  @ManyToOne(fetch = FetchType.LAZY)
  private Stock stock;

  public StockLine(Long price, StockLineStrength stockLineStrength,
      StockLineType lineType, Stock stock) {
    this.price = price;
    this.stockLineStrength = stockLineStrength;
    this.lineType = lineType;
    this.stock = stock;
  }

  public static StockLine resistance(long closePrice, Long currentVolume, Long averageDailyVolume, Stock stock) {
    return new StockLine(
        closePrice,
        StockLineStrength.create(currentVolume, averageDailyVolume),
        StockLineType.RESISTANCE,
        stock
    );
  }

  public static StockLine support(long closePrice, Long currentVolume, Long averageDailyVolume, Stock stock) {
    return new StockLine(
        closePrice,
        StockLineStrength.create(currentVolume, averageDailyVolume),
        StockLineType.SUPPORT,
        stock
    );
  }

  public void updateStrength(Long additionalVolume, Long averageDailyVolume) {
    this.stockLineStrength.touch(additionalVolume, averageDailyVolume);
  }
}
