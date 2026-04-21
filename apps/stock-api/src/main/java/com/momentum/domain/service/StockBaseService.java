package com.momentum.domain.service;

import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.entity.indicator.price.StockPricePoint;
import com.momentum.domain.entity.indicator.price.StockPricePointType;
import com.momentum.domain.respository.StockBaseRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockBaseService {

  public static final double PRICE_SIMILARITY_THRESHOLD = 2.0;
  public static final double BASE_BOUNDARY_THRESHOLD = 5.0;

  private final StockBaseRepository stockBaseRepository;
  private final StockBaseInitializer initializer;
  private final StockBasePivotLowHandler pivotLowHandler;
  private final StockBasePivotHighHandler pivotHighHandler;

  @Transactional
  public void resolve(StockPricePoint confirmedPricePoint) {
    if (confirmedPricePoint == null) {
      throw new IllegalArgumentException("stockPivot cannot be null");
    }
    if (StockPricePointType.isNonPivot(confirmedPricePoint)) {
      return;
    }

    Optional<StockBase> currentBaseOpt = stockBaseRepository.findCurrentBaseWithLines(
        confirmedPricePoint.getStock().getId());
    if (currentBaseOpt.isEmpty()) {
      initializer.resolve(confirmedPricePoint);
      return;
    }

    StockBase currentBase = currentBaseOpt.get();
    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_LOW)) {
      pivotLowHandler.resolve(confirmedPricePoint, currentBase, PRICE_SIMILARITY_THRESHOLD, BASE_BOUNDARY_THRESHOLD);
    }
    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_HIGH)) {
      pivotHighHandler.resolve(confirmedPricePoint, currentBase, PRICE_SIMILARITY_THRESHOLD, BASE_BOUNDARY_THRESHOLD);
    }
  }
}
