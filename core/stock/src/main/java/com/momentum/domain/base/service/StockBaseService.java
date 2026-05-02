package com.momentum.domain.base.service;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseService {

  public static final double BASE_BOUNDARY_THRESHOLD = 5.0;

  private final StockBaseInitializer stockBaseInitializer;
  private final StockBaseConfirmer stockBaseConfirmer;
  private final StockBasePointIntegrator stockBasePointIntegrator;
  private final StockBaseStageLevelAdjuster stockBaseStageLevelAdjuster;
  private final StockBaseLineTypeConvertor stockBaseLineTypeConvertor;

  private final StockBaseRepository stockBaseRepository;

  public void resolve(List<StockPricePoint> typeConfirmedPoints) {
    if (typeConfirmedPoints == null || typeConfirmedPoints.isEmpty()) {
      throw new IllegalArgumentException("stockPricePoint cannot be null");
    }
    StockPricePoint confirmedPricePoint = typeConfirmedPoints.getFirst();
    if (StockPricePointType.isNonPivot(confirmedPricePoint)) {
      return;
    }
    Optional<StockBase> currentBaseOpt = stockBaseRepository.findCurrentBaseWithLines(confirmedPricePoint.getStock());
    if (currentBaseOpt.isEmpty()) {
      stockBaseInitializer.resolve(confirmedPricePoint);
      return;
    }
    confirmStockBase(confirmedPricePoint, currentBaseOpt.get());
    stockBasePointIntegrator.resolve(confirmedPricePoint, currentBaseOpt.get(), BASE_BOUNDARY_THRESHOLD);
    stockBaseStageLevelAdjuster.resolve(confirmedPricePoint, currentBaseOpt.get(), BASE_BOUNDARY_THRESHOLD);
  }

  private void confirmStockBase(StockPricePoint confirmedPricePoint, StockBase currentBase) {
    StockBase newBase = stockBaseConfirmer.resolve(confirmedPricePoint, currentBase, BASE_BOUNDARY_THRESHOLD);
    stockBaseLineTypeConvertor.convertLineType(newBase);
  }
}
