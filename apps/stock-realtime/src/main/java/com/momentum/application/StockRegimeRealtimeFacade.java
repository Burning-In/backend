package com.momentum.application;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockCode;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stocktick.StockStateChangedEvent;
import com.momentum.domain.stocktick.StockTick;
import com.momentum.domain.stocktick.StockTickRepository;
import com.momentum.infrastructure.StockTickInfo;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockRegimeRealtimeFacade {

  public static final Double NOISE_THRESHOLD_PERCENT = 2.0;

  private final StockRepository stockRepository;
  private final StockBaseRepository stockBaseRepository;
  private final StockTickRepository stockTickRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  // # 클래스 분리 필요
  @Transactional
  public void processTick(StockTickInfo stockTickInfo, Instant now) {
    StockCode stockCode = StockCode.getCode(stockTickInfo.stockCode());
    Stock stock = stockRepository.findByStockCode(stockCode.getCode())
        .orElseThrow(IllegalStateException::new);
    StockBase stockBase = stockBaseRepository.findCurrentBaseWithLines(stock.getId())
        .orElseThrow(IllegalArgumentException::new);

    // # 상승돌파
    if (calculateGap(stockBase.getHighestResistancePrice(), stockTickInfo.currentPrice()) >= NOISE_THRESHOLD_PERCENT
        && stock.getStockTrend().equals(StockTrend.UPTREND) && !stock.getStockRegime()
        .equals(StockRegime.BREAKOUT_START)) {
      StockTick stockTick = stockTickRepository.findDailyFirst(now).orElseThrow(IllegalStateException::new);
      Double beforeResistanceAverage = stockTickRepository.averageDailyOrderFlow(now, stockTick.getPrice(),
          stockBase.getHighestResistancePrice() - 1);
      Double afterResistanceAverage = stockTickRepository.averageDailyOrderFlow(now, stockBase.getHighestResistancePrice(),
          stockTickInfo.currentPrice());

      if (beforeResistanceAverage < afterResistanceAverage) {
        StockRegime prevState = stock.getStockRegime();
        stock.update(StockRegime.BREAKOUT_START);
        stockRepository.save(stock);
        StockStateChangedEvent stockStateChangedEvent = new StockStateChangedEvent(stockCode.getCode(), prevState,
            StockRegime.BREAKOUT_START);
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
        StockRegime prevState = stock.getStockRegime();
        stock.update(StockRegime.BREAKOUT_FAILED);
        stockRepository.save(stock);
        StockStateChangedEvent stockStateChangedEvent = new StockStateChangedEvent(stockCode.getCode(), prevState,
            StockRegime.BREAKOUT_FAILED);
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
        StockRegime prevState = stock.getStockRegime();
        stock.update(StockRegime.DOWNSIDE_BREAK);
        stockRepository.save(stock);
        StockStateChangedEvent stockStateChangedEvent = new StockStateChangedEvent(stockCode.getCode(), prevState,
            StockRegime.DOWNSIDE_BREAK);
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
}
