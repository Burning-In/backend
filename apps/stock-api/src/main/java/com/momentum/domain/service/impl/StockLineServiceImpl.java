package com.momentum.domain.service.impl;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.entity.indicator.StockLine;
import com.momentum.domain.respository.StockLineRepository;
import com.momentum.domain.service.StockLineService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockLineServiceImpl implements StockLineService {

  private final StockLineRepository stockLineRepository;

  // # 고점이 새로 생겼을떄, 새로운 저항선인지 판단
  @Override
  public StockLine determineResistance(Stock stock, StockCandle highPivotPoint, double thresholdPercent) {
    Optional<StockLine> matchedResistance = stockLineRepository.findTopResistanceInRange(stock.getId(),
        highPivotPoint.getClosePrice(), thresholdPercent);
    if (matchedResistance.isEmpty()) {
      StockLine resistance = StockLine.resistance(highPivotPoint.getClosePrice(), stock);
      return stockLineRepository.save(resistance);
    }
    StockLine existingStockLine = matchedResistance.get();
    existingStockLine.increaseTouchCount();
    return stockLineRepository.save(existingStockLine);
  }
}
