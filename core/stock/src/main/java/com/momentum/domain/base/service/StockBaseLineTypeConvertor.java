package com.momentum.domain.base.service;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.base.entity.StockBaseLine;
import com.momentum.domain.base.entity.StockBaseLineType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseLineTypeConvertor {

  private final StockBaseRepository stockBaseRepository;

  public void convertLineType(StockBase previousBase, StockBase newBase) {
    if (previousBase == null || newBase == null) {
      return;
    }
    for (StockBaseLine line : previousBase.getStockBaseLines()) {
      if (line.getType() == StockBaseLineType.RESISTANCE
          && line.getPrice() <= newBase.getLowestSupportLine().getPrice()) {
        line.convertLineType();
      }
      if (line.getType() == StockBaseLineType.SUPPORT
          && line.getPrice() >= newBase.getHighestResistanceLine().getPrice()) {
        line.convertLineType();
      }
    }
    stockBaseRepository.save(previousBase);
  }
}
