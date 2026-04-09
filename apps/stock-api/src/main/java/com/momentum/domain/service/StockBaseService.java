package com.momentum.domain.service;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.entity.indicator.price.StockBaseType;
import com.momentum.domain.entity.indicator.price.StockLine;
import com.momentum.domain.entity.indicator.price.StockLineType;
import com.momentum.domain.respository.StockBaseRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockBaseService {

  private final StockBaseRepository stockBaseRepository;

  @Transactional
  public StockBase createCandidate(Stock stock, StockLine triggerLine) {
    Optional<StockBase> previousBase = stockBaseRepository.findLastBase(stock.getId(), StockBaseType.CONFIRMED);
    long previousBaseAccCount = previousBase
        .map(StockBase::getAccumulationCount)
        .orElse(0L);
    StockBase candidate = StockBase.createCandidate(stock, triggerLine, previousBaseAccCount);

    return stockBaseRepository.save(candidate);
  }

  // 리펙토링 필요
  @Transactional
  public void evaluateBase(Stock stock, StockLine firstLineAfterCandidate) {
    StockBase candidate = stockBaseRepository
        .findLastBase(stock.getId(), StockBaseType.CANDIDATE)
        .orElseThrow(() -> new IllegalStateException("No candidate base found"));
    // 여기에서 에러가 생기긴하네
    StockBase previousConfirmed = stockBaseRepository
        .findLastBase(stock.getId(), StockBaseType.CONFIRMED)
        .orElse(null);

    // PIVOT_LOW → 첫저점 (저항 형성 이후)
    if (firstLineAfterCandidate.getLineType().equals(StockLineType.SUPPORT)) {
      // 지지선 위에서 형성 → 병합 조건 (상승 유지 실패)
      if (previousConfirmed != null &&
          firstLineAfterCandidate.getPrice() < previousConfirmed.getHighestResistancePrice()) {
        previousConfirmed.merge(candidate, firstLineAfterCandidate);
        stockBaseRepository.save(previousConfirmed);
        candidate.delete();
        stockBaseRepository.save(candidate);
      } else {
        // 정상 범위라면 → confirm
        candidate.confirm(firstLineAfterCandidate);
        stockBaseRepository.save(candidate);
      }
    }

    // PIVOT_HIGH → 첫고점 (지지선 하락후 첫지지 형성 이후)
    if (firstLineAfterCandidate.getLineType().equals(StockLineType.RESISTANCE)) {
      // 지지선 아래로 깨짐 → 병합 조건 (하락)
      if (previousConfirmed != null &&
          firstLineAfterCandidate.getPrice() < previousConfirmed.getLowestSupportLinePrice()) {
        previousConfirmed.merge(candidate, firstLineAfterCandidate);
        stockBaseRepository.save(previousConfirmed);
        candidate.delete();
        stockBaseRepository.save(candidate);
      } else {
        // 정상 범위 → 후보 업데이트
        candidate.confirm(firstLineAfterCandidate);
        stockBaseRepository.save(candidate);
      }
    }
  }
}
