package com.momentum.application.insight;

import com.momentum.domain.ma.StockMovingAverage;
import com.momentum.domain.ma.StockMovingAveragePeriod;
import com.momentum.domain.ma.StockMovingAverageRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.MovingAverageResponse;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovingAverageInsightService {

  private final StockMovingAverageRepository stockMovingAverageRepository;
  private final StockCandleRepository stockCandleRepository;

  public MovingAverageResponse query(Stock stock, LocalDate at) {
    StockDailyCandle candle = stockCandleRepository.findRecentCandle(stock, at)
        .orElseThrow();
    long currentPrice = candle.getClosePrice();

    List<StockMovingAverage> maList = stockMovingAverageRepository.findLatestByStock(stock);
    Map<StockMovingAveragePeriod, Long> maMap = new EnumMap<>(StockMovingAveragePeriod.class);
    maList.forEach(ma -> maMap.put(ma.getStockMovingAveragePeriod(), ma.getMa()));

    Long ma50 = maMap.get(StockMovingAveragePeriod.MA_50);
    Long ma150 = maMap.get(StockMovingAveragePeriod.MA_150);
    Long ma200 = maMap.get(StockMovingAveragePeriod.MA_200);

    boolean isAboveMa50 = ma50 != null && currentPrice > ma50;
    boolean isMa50AboveMa150 = ma50 != null && ma150 != null && ma50 > ma150;
    boolean isMa150AboveMa200 = ma150 != null && ma200 != null && ma150 > ma200;

    return new MovingAverageResponse(
        currentPrice, ma50, ma150, ma200,
        isAboveMa50, isMa50AboveMa150, isMa150AboveMa200
    );
  }
}
