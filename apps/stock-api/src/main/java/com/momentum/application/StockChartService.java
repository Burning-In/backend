package com.momentum.application;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.movingaverage.StockMovingAveragePeriod;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.interfaces.api.stock.StockChartV1Dto.BaseListResponse;
import com.momentum.interfaces.api.stock.StockChartV1Dto.BaseListResponse.BaseItem;
import com.momentum.interfaces.api.stock.StockChartV1Dto.DailyCandleResponse;
import com.momentum.interfaces.api.stock.StockChartV1Dto.DailyCandleResponse.DailyCandle;
import com.momentum.interfaces.api.stock.StockChartV1Dto.MovingAverageResponse;
import com.momentum.interfaces.api.stock.StockChartV1Dto.MovingAverageResponse.MovingAverageItem;
import com.momentum.support.error.CoreException;
import com.momentum.support.error.ErrorType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockChartService {

  private final StockRepository stockRepository;
  private final StockCandleRepository stockCandleRepository;
  private final StockBaseRepository stockBaseRepository;

  public DailyCandleResponse getDailyCandles(String stockCode, LocalDate from, LocalDate to) {
    List<DailyCandle> candles = stockCandleRepository.findByStockAndDateRange(findStock(stockCode), from, to).stream()
        .map(candle -> new DailyCandle(
            candle.getTradeDate(),
            candle.getOpenPrice(),
            candle.getHighPrice(),
            candle.getLowPrice(),
            candle.getClosePrice(),
            candle.getVolume()))
        .toList();
    return new DailyCandleResponse(candles);
  }

  public MovingAverageResponse getMovingAverages(String stockCode, StockMovingAveragePeriod period,
      LocalDate from, LocalDate to) {
    Stock stock = findStock(stockCode);
    int window = period.getPeriod();
    // from 이전 lookback까지 포함해야 하므로 to까지의 전체 캔들을 tradeDate 오름차순으로 가져와 롤링 계산한다.
    List<StockDailyCandle> candles = stockCandleRepository.findByStockAndDateRange(stock, null, to);

    List<MovingAverageItem> dataPoints = new ArrayList<>();
    long windowSum = 0;
    for (int i = 0; i < candles.size(); i++) {
      windowSum += candles.get(i).getClosePrice();
      if (i >= window) {
        windowSum -= candles.get(i - window).getClosePrice();
      }
      if (i < window - 1) {
        continue; // 아직 N거래일치 데이터가 모이지 않음
      }
      LocalDate tradeDate = candles.get(i).getTradeDate();
      if (tradeDate.isBefore(from) || tradeDate.isAfter(to)) {
        continue; // 요청 구간 밖 (lookback 용도)
      }
      dataPoints.add(new MovingAverageItem(tradeDate, windowSum / window));
    }
    return new MovingAverageResponse(period, dataPoints);
  }

  public BaseListResponse getBases(String stockCode, LocalDate from, LocalDate to) {
    Stock stock = findStock(stockCode);
    List<StockBase> bases = stockBaseRepository.findAllByStockOrderByCreatedAt(stock);

    List<BaseItem> items = new ArrayList<>();
    for (int i = 0; i < bases.size(); i++) {
      StockBase base = bases.get(i);
      LocalDate startDate = base.getCreatedAt().toLocalDate();
      LocalDate endDate = (i + 1 < bases.size())
          ? bases.get(i + 1).getCreatedAt().toLocalDate()
          : null; // 가장 최신 베이스 = 진행 중
      if (startDate.isAfter(to) || (endDate != null && endDate.isBefore(from))) {
        continue;
      }
      items.add(new BaseItem(
          startDate,
          endDate,
          base.getLowestSupportLine().getPrice(),
          base.getHighestResistanceLine().getPrice()));
    }
    return new BaseListResponse(items);
  }

  private Stock findStock(String stockCode) {
    return stockRepository.findByStockCode(stockCode)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "종목을 찾을 수 없습니다: " + stockCode));
  }
}
