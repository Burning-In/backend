package com.momentum.domain.base.service;

import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseService {

  private final StockBaseInitializer stockBaseInitializer;
  private final StockBaseConfirmer stockBaseConfirmer;
  private final StockBasePointIntegrator stockBasePointIntegrator;
  private final StockBaseStageLevelAdjuster stockBaseStageLevelAdjuster;
  private final StockBaseLineTypeConvertor stockBaseLineTypeConvertor;

  private final StockBaseRepository stockBaseRepository;

  @Transactional
  public void resolve(StockAnchorPoint confirmedAnchorPoint) {
    if (confirmedAnchorPoint == null) {
      throw new IllegalArgumentException("confirmedAnchorPoint cannot be null");
    }

    if (confirmedAnchorPoint.getType().isNonPivot()) {
      return;
    }

    Optional<StockBase> currentBaseOpt = stockBaseRepository.findCurrentBaseWithLines(confirmedAnchorPoint.getStock());
    if (currentBaseOpt.isEmpty()) {
      stockBaseInitializer.resolve(confirmedAnchorPoint);
      return;
    }

    StockBase currentBase = currentBaseOpt.get();
    confirmStockBase(confirmedAnchorPoint, currentBase);
    stockBasePointIntegrator.resolve(confirmedAnchorPoint, currentBase);
    stockBaseStageLevelAdjuster.resolve(confirmedAnchorPoint, currentBase);
  }

  private void confirmStockBase(StockAnchorPoint confirmedAnchorPoint, StockBase currentBase) {
    StockBase newBase = stockBaseConfirmer.resolve(confirmedAnchorPoint, currentBase);
    stockBaseLineTypeConvertor.convertLineType(currentBase, newBase);
  }
}
