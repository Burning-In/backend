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

  public void convertLineType(StockBase newBase) {
    if (newBase == null) {
      return;
    }
    StockBase previousBase = stockBaseRepository.findCurrentBaseWithLines(newBase.getStock())
        .orElseThrow(IllegalArgumentException::new);
    for (StockBaseLine line : previousBase.getStockBaseLines()) {
      if (line.getLineType() == StockBaseLineType.RESISTANCE
          && line.getPrice() <= newBase.getLowestSupportLinePrice()) {
        line.convertLineType();
      }
      if (line.getLineType() == StockBaseLineType.SUPPORT
          && line.getPrice() >= newBase.getHighestResistancePrice()) {
        line.convertLineType();
      }
    }
    stockBaseRepository.save(previousBase);
  }
}
