package com.momentum.domain.entity.indicator.price;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.Stock;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockBase extends BaseEntity {

  private Long highestResistancePrice;
  private Long lowestSupportLinePrice;

  private Long strongestResistanceLinePrice;
  private Long strongestSupportLinePrice;
  private StockBaseKind stockBaseKind;

  private long stageLevel;

  @ManyToOne
  private Stock stock;

  @OneToMany(mappedBy = "stockBase", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<StockBaseLine> stockBaseLines;

  @OneToMany(mappedBy = "stockBase", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<StockPricePoint> stockPricePoints;

  // 눌림이면 표현을 할건지 안할건지
  public StockBase(Long highestResistancePrice, Long lowestSupportLinePrice,
      Long strongestResistanceLinePrice, Long strongestSupportLinePrice, StockBaseKind stockBaseKind,
      long stageLevel, Stock stock, List<StockBaseLine> stockBaseLines) {
    this.highestResistancePrice = highestResistancePrice;
    this.lowestSupportLinePrice = lowestSupportLinePrice;
    this.strongestResistanceLinePrice = strongestResistanceLinePrice;
    this.strongestSupportLinePrice = strongestSupportLinePrice;
    this.stockBaseKind = stockBaseKind;
    this.stageLevel = stageLevel;
    this.stock = stock;
    this.stockBaseLines = stockBaseLines;
    this.stockPricePoints = new ArrayList<>();
  }

  public static StockBase create(StockPricePoint highPricePoint, StockPricePoint lowPricePoint,
      long currentStageLevel, long averageDailyVolume) {
    StockBase stockBase = new StockBase(
        highPricePoint.getPrice(),
        lowPricePoint.getPrice(),
        highPricePoint.getPrice(),
        lowPricePoint.getPrice(),
        StockBaseKind.PULLBACK,
        currentStageLevel,
        highPricePoint.getStock(),
        new ArrayList<>()
    );
    StockBaseLine resistance = StockBaseLine.resistance(highPricePoint.getPrice(), highPricePoint.getVolume(),
        averageDailyVolume, stockBase);
    StockBaseLine support = StockBaseLine.support(lowPricePoint.getPrice(), lowPricePoint.getVolume(),
        averageDailyVolume, stockBase);
    stockBase.stockBaseLines.addAll(List.of(resistance, support));
    highPricePoint.assignBase(stockBase);
    lowPricePoint.assignBase(stockBase);
    stockBase.stockPricePoints.addAll(List.of(highPricePoint, lowPricePoint));

    return stockBase;
  }

  public void update(long lastBaseStageLevel) {
    this.stageLevel = lastBaseStageLevel;
  }

  // 동일한 좀 필터링 필요
  public void addPoints(List<StockPricePoint> points, long averageDailyVolume, double threshold) {
    if (points == null || points.isEmpty()) {
      return;
    }

    for (StockPricePoint point : points) {
      point.assignBase(this);
      this.stockPricePoints.add(point);

      Optional<StockBaseLine> matchedLine = findMatchedLine(point, threshold);
      if (matchedLine.isPresent()) {
        matchedLine.get().updateStrength(point.getVolume(), averageDailyVolume);
      } else {
        this.stockBaseLines.add(createLine(point, averageDailyVolume));
      }

      updateBoundary(point);
    }
  }

  private StockBaseLine createLine(StockPricePoint point, long averageDailyVolume) {
    if (point.getStockPricePointType() == StockPricePointType.PIVOT_HIGH) {
      return StockBaseLine.resistance(point.getPrice(), point.getVolume(), averageDailyVolume, this);
    }
    return StockBaseLine.support(point.getPrice(), point.getVolume(), averageDailyVolume, this);
  }

  private Optional<StockBaseLine> findMatchedLine(StockPricePoint point, double threshold) {
    return this.stockBaseLines.stream()
        .filter(line -> line.getLineType() == toLineType(point))
        .filter(line -> isWithinThreshold(line.getPrice(), point.getPrice(), threshold))
        .findFirst();
  }

  private StockLineType toLineType(StockPricePoint point) {
    if (point.getStockPricePointType() == StockPricePointType.PIVOT_HIGH) {
      return StockLineType.RESISTANCE;
    }
    return StockLineType.SUPPORT;
  }

  private boolean isWithinThreshold(long linePrice, long pointPrice, double threshold) {
    double diff = Math.abs(linePrice - pointPrice) / (double) linePrice;
    return diff <= threshold;
  }

  private void updateBoundary(StockPricePoint point) {
    if (point.getPrice() > this.highestResistancePrice) {
      this.highestResistancePrice = point.getPrice();
    }
    if (point.getPrice() < this.lowestSupportLinePrice) {
      this.lowestSupportLinePrice = point.getPrice();
    }
    updateStrongestLines();
  }

  private void updateStrongestLines() {
    this.stockBaseLines.stream()
        .filter(line -> line.getLineType() == StockLineType.RESISTANCE)
        .max(Comparator.comparing(line -> line.getStockLineStrength().getStrength()))
        .ifPresent(line -> this.strongestResistanceLinePrice = line.getPrice());

    this.stockBaseLines.stream()
        .filter(line -> line.getLineType() == StockLineType.SUPPORT)
        .max(Comparator.comparing(line -> line.getStockLineStrength().getStrength()))
        .ifPresent(line -> this.strongestSupportLinePrice = line.getPrice());
  }
}
