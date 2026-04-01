package com.momentum.domain.entity.indicator;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.Stock;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockBase extends BaseEntity {

  private Long highestResistancePrice;
  private Long lowestSupportLinePrice;
  private Long accumulationCount;

  @Embedded
  private StockBaseVolatility stockBaseVolatility;

  @ManyToOne
  private Stock stock;

  @OneToMany(mappedBy = "stockBase")
  private List<StockBaseLine> stockLines;

  public StockBase(Long highestResistancePrice, Long lowestSupportLinePrice, Long accumulationCount,
      Stock stock) {
    this.highestResistancePrice = highestResistancePrice;
    this.lowestSupportLinePrice = lowestSupportLinePrice;
    this.accumulationCount = accumulationCount;
    this.stock = stock;
    this.stockLines = new ArrayList<>();
  }
}
