package com.momentum.domain.entity.indicator;

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
public class StockLine extends BaseEntity {

  private Long price;
  private Long resistanceTouchCount;
  private Long supportTouchCount;

  @Enumerated(EnumType.STRING)
  private StockLineType lineType;

  @ManyToOne(fetch = FetchType.LAZY)
  private Stock stock;

  public StockLine(Long price, Long resistanceTouchCount, Long supportTouchCount,
      StockLineType lineType, Stock stock) {
    this.price = price;
    this.resistanceTouchCount = resistanceTouchCount;
    this.supportTouchCount = supportTouchCount;
    this.lineType = lineType;
    this.stock = stock;
  }

  public static StockLine resistance(long closePrice, Stock stock) {
    return new StockLine(
        closePrice,
        1L,
        0L,
        StockLineType.RESISTANCE,
        stock
    );
  }

  public static StockLine support(long closePrice, Stock stock) {
    return new StockLine(
        closePrice,
        0L,
        1L,
        StockLineType.SUPPORT,
        stock
    );
  }

  // 동시성 문제 해결 필요
  public void increaseResistanceTouch() {
    this.resistanceTouchCount++;
  }

  public void increaseSupportTouch() {
    this.supportTouchCount++;
  }
}
