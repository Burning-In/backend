package com.momentum.domain.base.service;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stockcandle.StockCandleRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 배치 돌리는 와중에 조회해도 되나?
@Component
@RequiredArgsConstructor
public class StockBaseService {

  public static final double PRICE_SIMILARITY_THRESHOLD = 2.0;
  public static final double BASE_BOUNDARY_THRESHOLD = 5.0;

  private final StockBaseInitializer stockBaseInitializer;
  private final StockBaseConfirmer stockBaseConfirmer;
  private final StockBasePointIntegrator stockBasePointIntegrator;
  private final StockBaseStageLevelAdjuster stockBaseStageLevelAdjuster;
  private final StockBaseLineTypeConvertor stockBaseLineTypeConvertor;

  private final StockBaseRepository stockBaseRepository;
  private final StockCandleRepository stockCandleRepository;

  // PricePoint의 타입이 확정이되면, 조건에따라 베이스를 확정하러 들어온다.
  public void resolve(List<StockPricePoint> typeConfirmedPoints) {
    if (typeConfirmedPoints == null || typeConfirmedPoints.isEmpty()) {
      throw new IllegalArgumentException("stockPricePoint cannot be null");
    }
    StockPricePoint confirmedPricePoint = typeConfirmedPoints.getFirst();
    if (StockPricePointType.isNonPivot(confirmedPricePoint)) {
      return;
    }
    LocalDate oneYearAgo = LocalDate.now().minusYears(1); // 베이스 내의 거래량으로 가야됨, 1년이 아니라
    long averageDailyVolume = stockCandleRepository.findAvgVolume(confirmedPricePoint.getStock(), oneYearAgo);
    Optional<StockBase> currentBaseOpt = stockBaseRepository.findCurrentBaseWithLines(confirmedPricePoint.getStock());
    if (currentBaseOpt.isEmpty()) {
      stockBaseInitializer.resolve(confirmedPricePoint, averageDailyVolume);
      return;
    }
    // 1. 베이스 생성
    StockBase currentBase = currentBaseOpt.get();
    confirmStockBase(confirmedPricePoint, currentBase, averageDailyVolume);

    // 2. 기존베이스에 점 통합
    stockBasePointIntegrator.resolve(confirmedPricePoint, currentBase, PRICE_SIMILARITY_THRESHOLD,
        BASE_BOUNDARY_THRESHOLD, averageDailyVolume);

    // 3. stageLevel 통합
    stockBaseStageLevelAdjuster.resolve(confirmedPricePoint, currentBase, BASE_BOUNDARY_THRESHOLD);
  }

  private void confirmStockBase(StockPricePoint confirmedPricePoint, StockBase currentBase, long averageDailyVolume) {
    StockBase newBase = stockBaseConfirmer.resolve(confirmedPricePoint, currentBase, PRICE_SIMILARITY_THRESHOLD,
        averageDailyVolume);
    stockBaseLineTypeConvertor.convertLineType(newBase);
  }
}
