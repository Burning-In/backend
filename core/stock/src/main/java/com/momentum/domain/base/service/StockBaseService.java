package com.momentum.domain.base.service;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stockcandle.StockCandleRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
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
  private final StockUpperBaseResolver stockUpperBaseResolver;
  private final StockLowerBaseResolver stockLowerBaseResolver;
  private final StockBaseLineTypeConvertor stockBaseLineTypeConvertor;

  private final StockBaseRepository stockBaseRepository;
  private final StockCandleRepository stockCandleRepository;

  // 만약에 PricePoint의 타입이 확정이되면, 여기로 들어옴
  // 클래스를 잘못 생각함 -> 1. 베이스 생성할떄, 2. 기존베이스 베이스에 점 통합할떄, 3. 현재베이스 -> 이전 베이스 스테이지 레벨 조정할떄
  // 새로운 베이스 일떄만 previousBase 라인타입 조정해야함
  @Transactional
  public void resolve(StockPricePoint confirmedPricePoint) {
    if (confirmedPricePoint == null) {
      throw new IllegalArgumentException("stockPivot cannot be null");
    }
    if (StockPricePointType.isNonPivot(confirmedPricePoint)) {
      return;
    }
    long averageDailyVolume = stockCandleRepository.findAvgVolumeByStockAndDateAfter(
        confirmedPricePoint.getStock(),
        LocalDate.now().minusYears(1));
    Optional<StockBase> currentBaseOpt = stockBaseRepository.findCurrentBaseWithLines(
        confirmedPricePoint.getStock().getId());
    StockBase newBase = null;
    if (currentBaseOpt.isEmpty()) {
      newBase = stockBaseInitializer.resolve(confirmedPricePoint, averageDailyVolume);
    }
    if (currentBaseOpt.isPresent()) {
      StockBase currentBase = currentBaseOpt.get();
      if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_LOW)) {
        newBase = stockUpperBaseResolver.resolve(confirmedPricePoint, currentBase, PRICE_SIMILARITY_THRESHOLD, BASE_BOUNDARY_THRESHOLD, averageDailyVolume);
      }
      if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_HIGH)) {
        newBase = stockLowerBaseResolver.resolve(confirmedPricePoint, currentBase, PRICE_SIMILARITY_THRESHOLD, BASE_BOUNDARY_THRESHOLD, averageDailyVolume);
      }
    }

    // 이전 베이스 라인 타입 컨버터 -> 다만, 새로운 베이스가 생겼을때에만 사용가능(지금은 좀 섞여있음, newBase가 아니기도함) -> 반환값은 previousBase
    // 도메인 서비스에서 반환만 할지 아니면, 저장까지할지
    // 있으면 진행해야함, 아니 근데 이전에 베이스라고 해야지 여기 메서드는 왜 current냐
    if (newBase != null) {
      StockBase previousBase = stockBaseRepository.findCurrentBaseWithLines(confirmedPricePoint.getStock().getId())
          .orElseThrow(IllegalArgumentException::new);
      stockBaseLineTypeConvertor.convertLineType(previousBase, newBase);
      stockBaseRepository.save(previousBase);
    }

    stockBaseRepository.save(newBase);
  }
}
