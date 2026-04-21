package com.momentum.domain.entity.analysis.base;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
  private StockBaseLineStrength stockBaseLineStrength;

  @Enumerated(EnumType.STRING)
  private StockBaseLineType lineType;

  @ManyToOne
  private StockBase stockBase;

  private StockBaseLine(Long price, StockBaseLineStrength stockBaseLineStrength,
      StockBaseLineType lineType, StockBase stockBase) {
    this.price = price;
    this.stockBaseLineStrength = stockBaseLineStrength;
    this.lineType = lineType;
    this.stockBase = stockBase;
  }

  public static StockBaseLine resistance(long closePrice, Long currentVolume, Long averageDailyVolume, StockBase stockBase) {
    return new StockBaseLine(
        closePrice,
        StockBaseLineStrength.create(currentVolume, averageDailyVolume),
        StockBaseLineType.RESISTANCE,
        stockBase
    );
  }

  public static StockBaseLine support(long closePrice, Long currentVolume, Long averageDailyVolume, StockBase stockBase) {
    return new StockBaseLine(
        closePrice,
        StockBaseLineStrength.create(currentVolume, averageDailyVolume),
        StockBaseLineType.SUPPORT,
        stockBase
    );
  }

  public void updateStrength(Long additionalVolume, Long averageDailyVolume) {
    this.stockBaseLineStrength.touch(additionalVolume, averageDailyVolume);
  }

  // 저항 ↔ 지지 타입 전환
  public void convertLineType() {
    if (this.lineType == StockBaseLineType.RESISTANCE) {
      this.lineType = StockBaseLineType.SUPPORT;
    } else {
      this.lineType = StockBaseLineType.RESISTANCE;
    }
  }
}
