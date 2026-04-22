package com.momentum.domain.entity.score;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.stock.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockRankScore extends BaseEntity {

  private BigDecimal momentum; // 12개월 모멘텀 = (현재가 - 12개월전가) / 12개월전가

  private BigDecimal fip;      // FIP = Sign(12개월 수익률) × [(하락일수 / 252) - (상승일수 / 252)]

  private LocalDate baseDate; // 계산 기준일 (당일)

  @ManyToOne
  private Stock stock;

  private StockRankScore(BigDecimal momentum, BigDecimal fip, LocalDate baseDate, Stock stock) {
    this.momentum = momentum;
    this.fip = fip;
    this.baseDate = baseDate;
    this.stock = stock;
  }

  public static StockRankScore create(BigDecimal momentum, BigDecimal fip, LocalDate baseDate, Stock stock) {
    return new StockRankScore(momentum, fip, baseDate, stock);
  }
}
