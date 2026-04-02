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

  @OneToMany(mappedBy = "stockBase", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<StockBaseLine> stockBaseLines;

  public StockBase(Long highestResistancePrice, Long lowestSupportLinePrice, Long accumulationCount,
      StockBaseVolatility stockBaseVolatility, Stock stock, StockBaseType stockBaseType,
      List<StockBaseLine> stockBaseLines) {
    this.highestResistancePrice = highestResistancePrice;
    this.lowestSupportLinePrice = lowestSupportLinePrice;
    this.accumulationCount = accumulationCount;
    this.stockBaseVolatility = stockBaseVolatility;
    this.stock = stock;
    this.stockBaseType = stockBaseType;
    this.stockBaseLines = stockBaseLines;
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

  // 병합시 변동성 수정필요
  // 저항선을 잡을떄 못잡는다는건데
  public void merge(StockBase failedConfirmed, StockLine firstLineAfterCandidate) {
    if (failedConfirmed.highestResistancePrice != null) {
      this.highestResistancePrice = Math.max(failedConfirmed.highestResistancePrice, this.highestResistancePrice);
      if (this.lowestSupportLinePrice != null) {
        this.lowestSupportLinePrice = Math.min(firstLineAfterCandidate.getPrice(), this.lowestSupportLinePrice);
      } else {
        this.lowestSupportLinePrice = firstLineAfterCandidate.getPrice();
      }
    }
    if (failedConfirmed.lowestSupportLinePrice != null) {
      this.lowestSupportLinePrice = Math.min(failedConfirmed.lowestSupportLinePrice, this.lowestSupportLinePrice);
      if (this.highestResistancePrice != null) {
        this.highestResistancePrice = Math.max(firstLineAfterCandidate.getPrice(), this.highestResistancePrice);
      } else {
        this.highestResistancePrice = firstLineAfterCandidate.getPrice();
      }
    }

    for (StockBaseLine stockBaseLine : failedConfirmed.stockBaseLines) {
      stockBaseLine.changeBase(this);
      this.stockBaseLines.add(stockBaseLine);
    }
    this.updateBaseLine(firstLineAfterCandidate);
  }

  public void confirm(StockLine triggerLine) {
    this.stockBaseType = StockBaseType.CONFIRMED;
    if (this.highestResistancePrice != null && triggerLine.getPrice() != null) {
      this.highestResistancePrice = Math.max(triggerLine.getPrice(), this.highestResistancePrice);
    }
    if (this.highestResistancePrice == null) {
      this.highestResistancePrice = triggerLine.getPrice();
    }
    if (this.lowestSupportLinePrice != null && triggerLine.getPrice() != null) {
      this.lowestSupportLinePrice = Math.min(triggerLine.getPrice(), this.lowestSupportLinePrice);
    }
    if (this.lowestSupportLinePrice == null && triggerLine.getPrice() != null) {
      this.lowestSupportLinePrice = triggerLine.getPrice();
    }
    StockBaseLine stockBaseLine = new StockBaseLine(this, triggerLine);
    this.stockBaseLines.add(stockBaseLine);
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
    this.stockBaseLines.add(baseLine);
  }
}
