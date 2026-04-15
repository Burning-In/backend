package com.momentum.domain.service;

import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.entity.indicator.price.StockBaseType;
import com.momentum.domain.entity.indicator.price.StockLine;
import com.momentum.domain.entity.indicator.price.StockPivot;
import com.momentum.domain.entity.indicator.price.StockPivotType;
import com.momentum.domain.respository.StockBaseRepository;
import com.momentum.domain.respository.StockCandleRepository;
import com.momentum.domain.respository.StockLineRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockLineService {

  private static final double PIVOT_THRESHOLD = 2.0;

  private final StockLineRepository stockLineRepository;
  private final StockBaseRepository stockBaseRepository;
  private final StockCandleRepository stockCandleRepository;

  public void resolveStockLine(StockPivot stockPivot) {
    if (stockPivot == null) {
      throw new IllegalStateException();
    }
    Long averageDailyVolume = stockCandleRepository.findAvgVolumeByStockAndDateAfter(stockPivot.getStock(),
        LocalDate.now().minusYears(1));
    Optional<StockLine> matchedResistance = stockLineRepository.findTopResistanceInRange(stockPivot.getStock().getId(),
        stockPivot.getPrice(), PIVOT_THRESHOLD);
    if (matchedResistance.isPresent()) {
      StockLine existingStockLine = matchedResistance.get();
      if (averageDailyVolume != null) {
        existingStockLine.updateStrength(stockPivot.getVolume(), averageDailyVolume);
      }
      stockLineRepository.save(existingStockLine);
      return;
    }

    StockLine stockLine = createStockLine(stockPivot, averageDailyVolume);
    if (stockLine != null) {
      stockLineRepository.save(stockLine);
    }
  }

  private StockLine createStockLine(StockPivot stockPivot, Long averageDailyVolume) {
    Optional<StockBase> lastBase = stockBaseRepository.findLastBase(stockPivot.getStock().getId(), StockBaseType.CONFIRMED);
    if (lastBase.isPresent()) {
      return createNewStockLine(stockPivot, averageDailyVolume, lastBase.get());
    }
    return createFirstStockLine(stockPivot, averageDailyVolume);

  }

  public StockLine createFirstStockLine(StockPivot stockPivot, Long averageDailyVolume) {
    if (stockPivot.getStockPivotType().equals(StockPivotType.PIVOT_HIGH)) {
      return StockLine.resistance(stockPivot.getPrice(), stockPivot.getVolume(), averageDailyVolume,
          stockPivot.getStock());
    }
    return StockLine.support(stockPivot.getPrice(), stockPivot.getVolume(), averageDailyVolume,
        stockPivot.getStock());
  }

  private StockLine createNewStockLine(StockPivot stockPivot, Long averageDailyVolume, StockBase lastBase) {
    if (lastBase.getStrongestResistanceLinePrice() < stockPivot.getPrice()
        && stockPivot.getStockPivotType().equals(StockPivotType.PIVOT_HIGH)) {
      return StockLine.resistance(stockPivot.getPrice(), stockPivot.getVolume(), averageDailyVolume,
          stockPivot.getStock());
    }
    if (lastBase.getStrongestSupportLinePrice() > stockPivot.getPrice()
        && stockPivot.getStockPivotType().equals(StockPivotType.PIVOT_LOW)) {
      return StockLine.support(stockPivot.getPrice(), stockPivot.getVolume(), averageDailyVolume,
          stockPivot.getStock());
    }

    return null;
  }
}
