package com.momentum.domain.service;

import static com.momentum.domain.service.StockRegimeRealTimeService.NOISE_THRESHOLD_PERCENT;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCode;
import com.momentum.domain.entity.StockDailyCandle;
import com.momentum.domain.entity.StockRegime;
import com.momentum.domain.entity.StockTrend;
import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.respository.StockBaseRepository;
import com.momentum.domain.respository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockDailyRegimeService {

  private final StockRepository stockRepository;
  private final StockBaseRepository stockBaseRepository;

  //-----------
  // # 일봉 데이터(보정 및 확정용)
  public void finalizeDailyState(StockDailyCandle stockDailyCandle) {
    StockCode stockCode = StockCode.getCode(stockDailyCandle.getStock().getCode());
    Stock stock = stockRepository.findByStockCode(stockCode.getCode())
        .orElseThrow(IllegalStateException::new);
    StockBase stockBase = stockBaseRepository.findCurrentBaseWithLines(stock.getId())
        .orElseThrow(IllegalArgumentException::new);

    // # 상승돌파
    if (calculateGap(stockBase.getHighestResistancePrice(), stockDailyCandle.getClosePrice()) > NOISE_THRESHOLD_PERCENT
        && stock.getStockTrend().equals(StockTrend.UPTREND) && !stock.getStockRegime().equals(StockRegime.BREAKOUT)) {
      stock.update(StockRegime.BREAKOUT);
      stockRepository.save(stock);
    }
    // # 상승가능
    // - 가격이 지지/저항선 안에 있고, vcp가 형성되어야함, 아리면 저항선에 가깝던가
    if (calculateGap(stockBase.getHighestResistancePrice(), stockDailyCandle.getClosePrice()) <= -NOISE_THRESHOLD_PERCENT &&
        stockBase.getLowestSupportLinePrice() < stockDailyCandle.getClosePrice() && stock.getStockTrend()
        .equals(StockTrend.UPTREND)
        && !stock.getStockRegime().equals(StockRegime.BREAKOUT_CANDIDATE)) {
//      if (stockBase.getStockBaseVolatility().isContracting()) {
//        stock.update(StockRegime.BREAKOUT_CANDIDATE);
//      }
      if (Math.abs(calculateGap(stockBase.getHighestResistancePrice(), stockDailyCandle.getClosePrice()))
          <= NOISE_THRESHOLD_PERCENT) {
        stock.update(StockRegime.BREAKOUT_CANDIDATE);
      }

      stockRepository.save(stock);
    }

    // # 하락가능
    if (calculateGap(stockBase.getHighestResistancePrice(), stockDailyCandle.getClosePrice()) <= -NOISE_THRESHOLD_PERCENT &&
        stockBase.getLowestSupportLinePrice() < stockDailyCandle.getClosePrice() &&
        !stock.getStockRegime().equals(StockRegime.FAILED_BREAKOUT)) {
      stock.update(StockRegime.FAILED_BREAKOUT);
      stockRepository.save(stock);
    }
    // # 하락
    if (calculateGap(stockBase.getLowestSupportLinePrice(), stockDailyCandle.getClosePrice()) <= -NOISE_THRESHOLD_PERCENT &&
        !stock.getStockRegime().equals(StockRegime.BREAKDOWN)) {
      stock.update(StockRegime.BREAKDOWN);
      stockRepository.save(stock);
    }
  }

  private double calculateGap(long linePrice, long currentPrice) {
    if (linePrice == 0.0) {
      return 0.0;
    }
    return ((double) (currentPrice - linePrice) / linePrice) * 100;
  }
}
