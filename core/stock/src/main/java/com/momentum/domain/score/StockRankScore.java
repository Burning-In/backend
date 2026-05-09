package com.momentum.domain.score;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockDailyCandle;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockRankScore extends BaseEntity {

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "momentum"))
  private Momentum momentum;

  @Embedded
  private FipScore fipScore;

  private LocalDate baseDate;

  @ManyToOne
  private Stock stock;

  private StockRankScore(Momentum momentum, FipScore fipScore, LocalDate baseDate, Stock stock) {
    this.momentum = momentum;
    this.fipScore = fipScore;
    this.baseDate = baseDate;
    this.stock = stock;
  }

  public static StockRankScore create(List<StockDailyCandle> candles, LocalDate baseDate, Stock stock) {
    Momentum momentum = Momentum.calculate(candles);
    FipScore fipScore = FipScore.calculate(momentum, candles);
    return new StockRankScore(momentum, fipScore, baseDate, stock);
  }

  public static StockRankScore create(Momentum momentum, FipScore fipScore, LocalDate baseDate, Stock stock) {
    return new StockRankScore(momentum, fipScore, baseDate, stock);
  }
}
