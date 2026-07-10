package com.momentum.domain.base.entity;

import static java.util.Objects.requireNonNull;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.stock.Stock;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockBase extends BaseEntity {

  private static final double BASE_VOLATILITY_THRESHOLD = 10.0;

  private long stageLevel;

  @Embedded
  private StockBaseVcp vcp;
  @Enumerated(EnumType.STRING)
  private StockBaseKind stockBaseKind;

  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private StockBaseLine highestResistanceLine;
  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private StockBaseLine lowestSupportLine;
  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private StockBaseLine strongestResistanceLine;
  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private StockBaseLine strongestSupportLine;

  @ManyToOne
  private Stock stock;

  @OneToMany(mappedBy = "stockBase", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
  private List<StockBaseLine> stockBaseLines;

  @OneToMany(mappedBy = "stockBase", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
  private List<StockPricePoint> stockPricePoints;

  private StockBase(StockPricePoint highPricePoint, StockPricePoint lowPricePoint, long stageLevel, long baseAverageVolume) {
    StockBaseLine resistance = StockBaseLine.create(highPricePoint, baseAverageVolume, this);
    StockBaseLine support = StockBaseLine.create(lowPricePoint, baseAverageVolume, this);
    this.stockBaseKind = requireNonNull(resolveBaseKind(highPricePoint.getPrice(), lowPricePoint.getPrice()));
    this.stageLevel = stageLevel;
    this.stock = requireNonNull(highPricePoint.getStock());
    this.stockPricePoints = new ArrayList<>(List.of(highPricePoint, lowPricePoint));
    this.vcp = new StockBaseVcp();
    this.highestResistanceLine = resistance;
    this.lowestSupportLine = support;
    this.strongestResistanceLine = resistance;
    this.strongestSupportLine = support;
    this.stockBaseLines = new ArrayList<>(List.of(resistance, support));
  }

  public static StockBase init(StockPricePoint highPricePoint, StockPricePoint lowPricePoint, long averageVolume) {
    return StockBase.create(highPricePoint, lowPricePoint, 1, averageVolume);
  }

  public static StockBase upper(StockPricePoint highPricePoint, StockPricePoint lowPricePoint,
      long currentStageLevel, long averageVolume) {
    return StockBase.create(highPricePoint, lowPricePoint, currentStageLevel + 1, averageVolume);
  }

  private static StockBase create(StockPricePoint highPricePoint, StockPricePoint lowPricePoint,
      long currentStageLevel, long baseAverageVolume) {
    StockBase stockBase = new StockBase(highPricePoint, lowPricePoint, currentStageLevel, baseAverageVolume);
    highPricePoint.assignBase(stockBase);
    lowPricePoint.assignBase(stockBase);
    return stockBase;
  }

  public void update(Long lastBaseStageLevel, List<Long> volatilityHistories) {
    if (volatilityHistories != null && !volatilityHistories.isEmpty()) {
      this.vcp.updateVcp(volatilityHistories);
    }
    if (lastBaseStageLevel != null && !lastBaseStageLevel.equals(this.stageLevel)) {
      this.stageLevel = lastBaseStageLevel;
    }
  }

  public void integratePoints(List<StockPricePoint> points, long averageVolume, double priceThreshold) {
    if (points == null || points.isEmpty()) {
      return;
    }
    Set<StockPricePoint> previousPoints = new HashSet<>(this.stockPricePoints);
    for (StockPricePoint point : points) {
      if (previousPoints.contains(point)) {
        continue;
      }
      point.assignBase(this);
      this.stockPricePoints.add(point);
      StockBaseLine line = updateLineStrengthOrCreate(averageVolume, priceThreshold, point);
      updateHighestLine(line);
      updateLowestLine(line);
      updateStrongestLine(line);
    }
    this.stockBaseKind = resolveBaseKind(this.highestResistanceLine.getPrice(), this.lowestSupportLine.getPrice());
  }

  private StockBaseLine updateLineStrengthOrCreate(long averageVolume, double priceThreshold, StockPricePoint point) {
    Optional<StockBaseLine> matchedLine = this.stockBaseLines.stream()
        .filter(line -> line.isMatched(point, priceThreshold))
        .findFirst();
    if (matchedLine.isPresent()) {
      matchedLine.get().updateStrength(point.getVolume(), averageVolume);
      return matchedLine.get();
    }

    StockBaseLine newLine = StockBaseLine.create(point, averageVolume, this);
    this.stockBaseLines.add(newLine);
    return newLine;
  }

  private void updateHighestLine(StockBaseLine line) {
    if (line.getLineType() == StockBaseLineType.RESISTANCE
        && line.getPrice() > this.highestResistanceLine.getPrice()) {
      this.highestResistanceLine = line;
    }
  }

  private void updateLowestLine(StockBaseLine line) {
    if (line.getLineType() == StockBaseLineType.SUPPORT
        && line.getPrice() < this.lowestSupportLine.getPrice()) {
      this.lowestSupportLine = line;
    }
  }

  private void updateStrongestLine(StockBaseLine line) {
    if (line.getLineType() == StockBaseLineType.RESISTANCE
        && line.isStrongerThan(this.strongestResistanceLine)) {
      this.strongestResistanceLine = line;
    }
    if (line.getLineType() == StockBaseLineType.SUPPORT
        && line.isStrongerThan(this.strongestSupportLine)) {
      this.strongestSupportLine = line;
    }
  }

  private StockBaseKind resolveBaseKind(long highPrice, long lowPrice) {
    double volatility = (double) (highPrice - lowPrice) / lowPrice * 100;
    if (volatility >= BASE_VOLATILITY_THRESHOLD) {
      return StockBaseKind.BASE;
    }
    return StockBaseKind.PULLBACK;
  }

  public boolean isVcp() {
    return vcp.isVcp();
  }

  public long getResistanceUpperBound(double threshold) {
    return highestResistanceLine.getUpperBound(threshold);
  }

  public long getResistanceLowerBound(double threshold) {
    return highestResistanceLine.getLowerBound(threshold);
  }

  public long getSupportUpperBound(double threshold) {
    return lowestSupportLine.getUpperBound(threshold);
  }

  public long getSupportLowerBound(double threshold) {
    return lowestSupportLine.getLowerBound(threshold);
  }
}
