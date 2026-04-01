package com.momentum.domain.entity.indicator;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockBaseLine extends BaseEntity {

  @ManyToOne
  private StockBase stockBase;

  @ManyToOne
  private StockLine stockLine;

  public StockBaseLine(StockBase stockBase, StockLine stockLine) {
    this.stockBase = stockBase;
    this.stockLine = stockLine;
  }
}
