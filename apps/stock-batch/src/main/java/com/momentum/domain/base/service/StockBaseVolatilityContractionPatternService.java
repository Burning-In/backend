package com.momentum.domain.base.service;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseVolatilityContractionPatternService {

  private final StockBaseRepository stockBaseRepository;

  public void calculateVolatilityContractionPattern(Long stockBaseId) {
    StockBase stockBase = stockBaseRepository.findWithAnchorPointsById(stockBaseId)
        .orElseThrow(IllegalArgumentException::new);
    List<StockAnchorPoint> points = stockBase.getStockAnchorPoints();
    if (points == null) {
      throw new IllegalStateException("StockAnchorPoints must not be null");
    }

    List<StockAnchorPoint> stockAnchorPoints = points.stream()
        .filter(point -> !point.getType().isNonPivot())
        .toList();

    List<Long> volatilityHistories = calculateVolatilityHistories(stockAnchorPoints);
    stockBase.update(null, volatilityHistories);
    stockBaseRepository.save(stockBase);
  }

  private List<Long> calculateVolatilityHistories(List<StockAnchorPoint> stockAnchorPoints) {
    int left = 0;
    int right = 0;
    List<Long> volatilityHistories = new ArrayList<>();
    while (left <= right && right < stockAnchorPoints.size()) {
      StockAnchorPoint leftAnchorPoint = stockAnchorPoints.get(left);
      StockAnchorPoint rightAnchorPoint = stockAnchorPoints.get(right);
      if (rightAnchorPoint.getType().equals(StockAnchorPointType.HIGH)) {
        left = right;
      }
      if (leftAnchorPoint.getType().equals(StockAnchorPointType.HIGH) &&
          rightAnchorPoint.getType().equals(StockAnchorPointType.LOW)) {
        volatilityHistories.add(leftAnchorPoint.getPrice() - rightAnchorPoint.getPrice());
      }
      right++;
    }
    return volatilityHistories;
  }
}
