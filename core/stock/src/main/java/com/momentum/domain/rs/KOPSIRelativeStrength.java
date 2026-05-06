package com.momentum.domain.rs;

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
public class KOPSIRelativeStrength extends BaseEntity {

  private int rsScore;

  @ManyToOne
  private Stock stock;
  @ManyToOne
  private KOSPI kospi;

  public KOPSIRelativeStrength(int rsScore, Stock stock, KOSPI kospi) {
    this.rsScore = rsScore;
    this.stock = stock;
    this.kospi = kospi;
  }
}
