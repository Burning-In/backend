package com.momentum.application;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.entity.indicator.price.StockLine;
import com.momentum.domain.respository.StockLineRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockLineService {

  private final StockLineRepository stockLineRepository;

  public StockLine determineResistance(Stock stock, StockCandle highPivotPoint, double thresholdPercent) {
    Optional<StockLine> matchedResistance = stockLineRepository.findTopResistanceInRange(stock.getId(),
        highPivotPoint.getClosePrice(), thresholdPercent);
    if (matchedResistance.isEmpty()) {
      StockLine resistance = StockLine.resistance(highPivotPoint.getClosePrice(), stock);
      return stockLineRepository.save(resistance);
    }
    StockLine existingStockLine = matchedResistance.get();
    existingStockLine.increaseResistanceTouch();
    return stockLineRepository.save(existingStockLine);
  }

  public StockLine determineSupport(Stock stock, StockCandle lowPivotPoint, double thresholdPercent) {
    Optional<StockLine> matchedSupport = stockLineRepository.findLowestSupportInRange(stock.getId(),
        lowPivotPoint.getClosePrice(), thresholdPercent);
    if (matchedSupport.isEmpty()) {
      StockLine resistance = StockLine.support(lowPivotPoint.getClosePrice(), stock);
      return stockLineRepository.save(resistance);
    }
    StockLine existingStockLine = matchedSupport.get();
    existingStockLine.increaseSupportTouch();
    return stockLineRepository.save(existingStockLine);
  }
}

// # 저항선(그제, 중요 지우지말것)
// - 주의사항 : 아직 만들어지지 않은 저항선을 주가가 오늘 통과를 해버리면 어떻게 하지?(이러면 서로 엇갈릴수도 있는데, 하루전에도 4%가 떨어졌으면 고점인데, 현재 주가가 그값을 넘어버리니깐 지지로 상태 변경을 해야함)

// # 주의할점
// - (추후에 이벤트로) 지지/저항 상태 변경은 주가 내려가거나 올라갈떄 가차없이 처리합니다. 베이스 상태랑 달라요
