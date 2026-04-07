package com.momentum.application;

import com.momentum.application.dto.StockTickInfo;
import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCode;
import com.momentum.domain.entity.StockState;
import com.momentum.domain.entity.StockTick;
import com.momentum.domain.entity.StockTrend;
import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.entity.indicator.price.StockBaseType;
import com.momentum.domain.respository.StockBaseRepository;
import com.momentum.domain.respository.StockRepository;
import com.momentum.domain.respository.StockTickRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockStateService {

  private static final Double NOISE_THRESHOLD_PERCENT = 2.0;

  private final StockRepository stockRepository;
  private final StockBaseRepository stockBaseRepository;
  private final StockTickRepository stockTickRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Transactional
  public void processTick(StockTickInfo stockTickInfo, Instant now) {
    StockCode stockCode = StockCode.getCode(stockTickInfo.stockCode());
    Stock stock = stockRepository.findByStockCode(stockCode.getCode())
        .orElseThrow(IllegalStateException::new);
    StockBase stockBase = stockBaseRepository.findLastBase(stock.getId(), StockBaseType.CONFIRMED)
        .orElseThrow(IllegalArgumentException::new);

    // # 상승돌파
    if (calculateGap(stockBase.getHighestResistancePrice(), stockTickInfo.currentPrice()) >= NOISE_THRESHOLD_PERCENT
        && stock.getStockTrend().equals(StockTrend.UPTREND) && !stock.getStockState().equals(StockState.BREAKOUT)) {
      StockTick stockTick = stockTickRepository.findDailyFirst(now).orElseThrow(IllegalStateException::new);
      Double beforeResistanceAverage = stockTickRepository.averageDailyOrderFlow(now, stockTick.getPrice(),
          stockBase.getHighestResistancePrice() - 1);
      Double afterResistanceAverage = stockTickRepository.averageDailyOrderFlow(now, stockBase.getHighestResistancePrice(),
          stockTickInfo.currentPrice());

      if (beforeResistanceAverage < afterResistanceAverage) {
        StockState prevState = stock.getStockState();
        stock.update(StockState.BREAKOUT);
        stockRepository.save(stock);
        StockStateChangedEvent stockStateChangedEvent = new StockStateChangedEvent(stockCode.getCode(), prevState,
            StockState.BREAKOUT);
        applicationEventPublisher.publishEvent(stockStateChangedEvent);
      }
    }

    // # 하락가능
    // 조건: 현재가 <= 저항선 × 0.98  AND  저항선이후 avg < 저항선이전 avg
    // 이평선/RS 불필요. BREAKOUT 상태에서도 적용 (→ 돌파 취소)
    if (calculateGap(stockBase.getHighestResistancePrice(), stockTickInfo.currentPrice()) <= -NOISE_THRESHOLD_PERCENT &&
        stockBase.getLowestSupportLinePrice() < stockTickInfo.currentPrice()) {
      StockTick dailyFirstTick = stockTickRepository.findDailyFirst(now).orElseThrow(IllegalStateException::new);
      // 저항선 이전: 장 시작 ~ 저항선 아래 구간
      Double beforeResistanceAverage = stockTickRepository.averageDailyOrderFlow(
          now, dailyFirstTick.getPrice(), stockBase.getHighestResistancePrice() - 1);
      // 저항선 이후: 저항선 이상 ~ 당일 고가 구간 (저항선 위에서 거래된 틱들)
      Double afterResistanceAverage = stockTickRepository.averageDailyOrderFlow(
          now, stockBase.getHighestResistancePrice(), stockTickInfo.highPrice());

      if (afterResistanceAverage < beforeResistanceAverage) {
        StockState prevState = stock.getStockState();
        stock.update(StockState.FAILED_BREAKOUT);
        stockRepository.save(stock);
        StockStateChangedEvent stockStateChangedEvent = new StockStateChangedEvent(stockCode.getCode(), prevState,
            StockState.FAILED_BREAKOUT);
        applicationEventPublisher.publishEvent(stockStateChangedEvent);
      }
    }

    // # 하락 붕괴 (지지선 아래)
    // 조건: 현재가 <= 지지선 × 0.98  AND  지지선이후 avg < 지지선이전 avg
    // 이평선/RS 불필요
    if (calculateGap(stockBase.getLowestSupportLinePrice(), stockTickInfo.currentPrice()) <= -NOISE_THRESHOLD_PERCENT) {
      StockTick dailyFirstTick = stockTickRepository.findDailyFirst(now).orElseThrow(IllegalStateException::new);
      // 지지선 이전: 지지선 위에서 거래된 틱들 (장 시작 ~ 지지선 이탈 전)
      Double beforeSupportAverage = stockTickRepository.averageDailyOrderFlow(
          now, stockBase.getLowestSupportLinePrice() + 1, stockTickInfo.highPrice());
      // 지지선 이후: 지지선 이하에서 거래된 틱들
      Double afterSupportAverage = stockTickRepository.averageDailyOrderFlow(
          now, stockTickInfo.lowPrice(), stockBase.getLowestSupportLinePrice());

      if (afterSupportAverage < beforeSupportAverage) {
        StockState prevState = stock.getStockState();
        stock.update(StockState.BREAKDOWN);
        stockRepository.save(stock);
        StockStateChangedEvent stockStateChangedEvent = new StockStateChangedEvent(stockCode.getCode(), prevState,
            StockState.BREAKDOWN);
        applicationEventPublisher.publishEvent(stockStateChangedEvent);
      }
    }

    StockTick stockTick = new StockTick(stockTickInfo.tradeTime(), stockTickInfo.currentPrice(), stockTickInfo.tradeVolume(),
        stockTickInfo.accumulatedVolume(), stockTickInfo.tradeStrength(), stockCode);
    stockTickRepository.save(stockTick);
  }


  private double calculateGap(long linePrice, long currentPrice) {
    if (linePrice == 0.0) {
      return 0.0;
    }
    return ((double) (currentPrice - linePrice) / linePrice) * 100;
  }


  //-----------
  // # 일봉 데이터(보정 및 확정용)
  // 일봉으로 데이터 바뀌는 것도 해야함
  // 이거 하고 일일 배치일떄는 어떻게 할건지 정하자 -> 일일 배치를 통해서는 어떻게 업데이트 할건가?
  public void finalizeDailyState() {

  }
}
