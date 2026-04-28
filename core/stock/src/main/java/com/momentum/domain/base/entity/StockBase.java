package com.momentum.domain.base.entity;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stock.Stock;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockBase extends BaseEntity {

  private static final double BASE_VOLATILITY_THRESHOLD = 10.0;

  private Long highestResistancePrice;
  private Long lowestSupportLinePrice;

  private Long strongestResistanceLinePrice;
  private Long strongestSupportLinePrice;
  private StockBaseKind stockBaseKind;

  private long stageLevel;
  private boolean isVcp;

  @ManyToOne
  private Stock stock;

  @OneToMany(mappedBy = "stockBase", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<StockBaseLine> stockBaseLines;

  @OneToMany(mappedBy = "stockBase", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<StockPricePoint> stockPricePoints;

  private StockBase(Long highestResistancePrice, Long lowestSupportLinePrice,
      Long strongestResistanceLinePrice, Long strongestSupportLinePrice, StockBaseKind stockBaseKind,
      long stageLevel, Stock stock, List<StockBaseLine> stockBaseLines, boolean isVcp) {
    this.highestResistancePrice = Objects.requireNonNull(highestResistancePrice);
    this.lowestSupportLinePrice = Objects.requireNonNull(lowestSupportLinePrice);
    this.strongestResistanceLinePrice = Objects.requireNonNull(strongestResistanceLinePrice);
    this.strongestSupportLinePrice = Objects.requireNonNull(strongestSupportLinePrice);
    this.stockBaseKind = Objects.requireNonNull(stockBaseKind);
    this.stageLevel = stageLevel;
    this.stock = Objects.requireNonNull(stock);
    this.stockBaseLines = Objects.requireNonNull(stockBaseLines);
    this.stockPricePoints = new ArrayList<>();
    this.isVcp = isVcp;
  }

  // 고점-저점 변동성이 10% 이상이면 BASE, 미만이면 PULLBACK
  public static StockBase create(StockPricePoint highPricePoint, StockPricePoint lowPricePoint,
      long currentStageLevel, long averageDailyVolume) {

    StockBaseKind kind = resolveKind(highPricePoint.getPrice(), lowPricePoint.getPrice());

    StockBase stockBase = new StockBase(
        highPricePoint.getPrice(),
        lowPricePoint.getPrice(),
        highPricePoint.getPrice(),
        lowPricePoint.getPrice(),
        kind,
        currentStageLevel,
        highPricePoint.getStock(),
        new ArrayList<>(),
        false
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

  // 점 추가 시 변동성이 10% 이상으로 바뀌면 PULLBACK → BASE로 전환
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

    this.stockBaseKind = resolveKind(this.highestResistancePrice, this.lowestSupportLinePrice);
  }

  public void updateVcp(List<Long> volatilityHistories) {
    if (volatilityHistories.size() == 1) {
      this.isVcp = false;
    } else if (volatilityHistories.size() == 2) {
      this.isVcp = volatilityHistories.get(0) > volatilityHistories.get(1);
    } else {
      this.isVcp = calculateSlope(movingAverage(volatilityHistories)) < 0;
    }
  }

  public boolean isPullback() {
    return this.stockBaseKind == StockBaseKind.PULLBACK;
  }

  // 고점-저점 변동성 10% 이상이면 BASE, 미만이면 PULLBACK
  private static StockBaseKind resolveKind(long highPrice, long lowPrice) {
    double volatility = (double) (highPrice - lowPrice) / lowPrice * 100;
    return volatility >= BASE_VOLATILITY_THRESHOLD ? StockBaseKind.BASE : StockBaseKind.PULLBACK;
  }

  private List<Double> movingAverage(List<Long> histories) {
    List<Double> result = new ArrayList<>();
    for (int i = 0; i < histories.size() - 1; i++) {
      result.add((histories.get(i) + histories.get(i + 1)) / 2.0);
    }
    return result;
  }

  private double calculateSlope(List<Double> values) {
    int n = values.size();
    double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
    for (int i = 0; i < n; i++) {
      sumX += i;
      sumY += values.get(i);
      sumXY += (double) i * values.get(i);
      sumX2 += (double) i * i;
    }
    return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
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

  private StockBaseLineType toLineType(StockPricePoint point) {
    if (point.getStockPricePointType() == StockPricePointType.PIVOT_HIGH) {
      return StockBaseLineType.RESISTANCE;
    }
    return StockBaseLineType.SUPPORT;
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
        .filter(line -> line.getLineType() == StockBaseLineType.RESISTANCE)
        .max(Comparator.comparing(line -> line.getStockBaseLineStrength().getStrength()))
        .ifPresent(line -> this.strongestResistanceLinePrice = line.getPrice());

    this.stockBaseLines.stream()
        .filter(line -> line.getLineType() == StockBaseLineType.SUPPORT)
        .max(Comparator.comparing(line -> line.getStockBaseLineStrength().getStrength()))
        .ifPresent(line -> this.strongestSupportLinePrice = line.getPrice());
  }
}
