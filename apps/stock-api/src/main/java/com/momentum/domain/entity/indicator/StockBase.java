package com.momentum.domain.entity.indicator;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.Stock;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

  @Enumerated(value = EnumType.STRING)
  private StockBaseType stockBaseType;

  @OneToMany(mappedBy = "stockBase", cascade = CascadeType.PERSIST)
  private List<StockBaseLine> stockLines;

  public StockBase(Long highestResistancePrice, Long lowestSupportLinePrice, Long accumulationCount,
      StockBaseVolatility stockBaseVolatility, Stock stock, StockBaseType stockBaseType,
      List<StockBaseLine> stockLines) {
    this.highestResistancePrice = highestResistancePrice;
    this.lowestSupportLinePrice = lowestSupportLinePrice;
    this.accumulationCount = accumulationCount;
    this.stockBaseVolatility = stockBaseVolatility;
    this.stock = stock;
    this.stockBaseType = stockBaseType;
    this.stockLines = stockLines;
  }

  public static StockBase createCandidate(Stock stock, StockLine triggerLine, long previousBaseCount) {
    StockBase candidate = new StockBase(
        initHighestResistancePrice(triggerLine),
        initLowestSupportPrice(triggerLine),
        initAccumulateCount(triggerLine, previousBaseCount),
        new StockBaseVolatility(0.0, 0.0, 0.0),
        stock,
        StockBaseType.CANDIDATE,
        new ArrayList<>()
    );
    candidate.updateBaseLine(triggerLine);
    return candidate;
  }

  private static Long initHighestResistancePrice(StockLine triggerLine) {
    if (triggerLine.getLineType().equals(StockLineType.RESISTANCE)) {
      return triggerLine.getPrice();
    }
    return null;
  }

  private static Long initLowestSupportPrice(StockLine triggerLine) {
    if (triggerLine.getLineType().equals(StockLineType.SUPPORT)) {
      return triggerLine.getPrice();
    }
    return null;
  }

  private static long initAccumulateCount(StockLine stockLine, long previousBaseCount) {
    if (stockLine.getLineType().equals(StockLineType.RESISTANCE)) {
      return previousBaseCount + 1;
    }
    return 1L;
  }

  private void updateBaseLine(StockLine stockLine) {
    StockBaseLine baseLine = new StockBaseLine(this, stockLine);
    this.stockLines.add(baseLine);
  }

  public void mergeStockBase(StockLine triggerLine) {
    StockBaseLine stockBaseLine = new StockBaseLine(this, triggerLine);
    this.stockLines.add(stockBaseLine);
  }
}
