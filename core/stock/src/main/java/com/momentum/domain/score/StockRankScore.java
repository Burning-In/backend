package com.momentum.domain.score;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.stock.Stock;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockRankScore extends BaseEntity {

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "momentum"))
  private MomentumScore momentumScore;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "fip"))
  private FrogInPanScore frogInPanScore;

  private LocalDate baseDate;

  @ManyToOne
  private Stock stock;

  private StockRankScore(MomentumScore momentumScore, FrogInPanScore frogInPanScore, LocalDate baseDate, Stock stock) {
    this.momentumScore = Objects.requireNonNull(momentumScore);
    this.frogInPanScore = Objects.requireNonNull(frogInPanScore);
    this.baseDate = Objects.requireNonNull(baseDate);
    this.stock = Objects.requireNonNull(stock);
  }

  public static StockRankScore create(List<Long> closePrices, LocalDate baseDate, Stock stock) {
    if (closePrices == null || closePrices.isEmpty()) {
      throw new IllegalArgumentException("closePrices cannot be null or empty");
    }
    long currentPrice = closePrices.getFirst();
    long pastPrice = closePrices.getLast();
    MomentumScore momentumScore = MomentumScore.calculate(currentPrice, pastPrice);
    FrogInPanScore frogInPanScore = FrogInPanScore.calculate(momentumScore.getValue(), closePrices);
    return new StockRankScore(momentumScore, frogInPanScore, baseDate, stock);
  }
}
