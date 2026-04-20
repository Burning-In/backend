package com.momentum.domain.service;

import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.entity.indicator.price.StockPricePoint;
import com.momentum.domain.entity.indicator.price.StockPricePointType;
import com.momentum.domain.respository.StockBaseRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseVolatilityContractionPatternService {

  private final StockBaseRepository stockBaseRepository;

  @Transactional
  public void calculateVolatilityContractionPattern(Long stockBaseId) {
    StockBase stockBase = stockBaseRepository.findWithPricePointsById(stockBaseId)
        .orElseThrow(IllegalArgumentException::new);
    List<StockPricePoint> points = stockBase.getStockPricePoints();
    if (points == null) {
      throw new IllegalStateException("StockPricePoints must not be null");
    }

    List<StockPricePoint> stockPricePoints = points.stream()
        .filter(point -> !StockPricePointType.isNonPivot(point))
        .toList();

    int left = 0;
    int right = 0;
    List<Long> volatilityHistories = new ArrayList<>();
    while (left <= right && right < stockPricePoints.size()) {
      StockPricePoint leftPricePoint = stockPricePoints.get(left);
      StockPricePoint rightPricePoint = stockPricePoints.get(right);
      if (rightPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_HIGH)) {
        left = right;
      }
      if (leftPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_HIGH) &&
          rightPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_LOW)) {
        volatilityHistories.add(leftPricePoint.getPrice() - rightPricePoint.getPrice());
      }
      right++;
    }

    stockBase.updateVcp(volatilityHistories);
    stockBaseRepository.save(stockBase);
  }
}
