package com.momentum.domain.relativestrength;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.stock.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KospiRelativeStrength extends BaseEntity {

  private int rsScore;

  @ManyToOne
  private Stock stock;
  @ManyToOne
  private Kospi kospi;

  public KospiRelativeStrength(int rsScore, Stock stock, Kospi kospi) {
    this.rsScore = rsScore;
    this.stock = stock;
    this.kospi = kospi;
  }
}
