package com.momentum.application;

import com.momentum.sharedkernel.StockMovingAveragePeriod;
import com.momentum.infrastructure.query.BaseRow;
import com.momentum.infrastructure.query.DailyCandleRow;
import com.momentum.infrastructure.query.StockChartQueryDao;
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

@Service
@RequiredArgsConstructor
public class StockChartService {

  private final StockChartQueryDao stockChartQueryDao;

  public DailyCandleResponse getDailyCandles(String stockCode, LocalDate from, LocalDate to) {
    requireStock(stockCode);
    List<DailyCandle> candles = stockChartQueryDao.findCandles(stockCode, from, to).stream()
        .map(row -> new DailyCandle(
            row.tradeDate(),
            row.openPrice(),
            row.highPrice(),
            row.lowPrice(),
            row.closePrice(),
            row.volume()))
        .toList();
    return new DailyCandleResponse(candles);
  }

  public MovingAverageResponse getMovingAverages(String stockCode, StockMovingAveragePeriod period,
      LocalDate from, LocalDate to) {
    requireStock(stockCode);
    int window = period.getPeriod();
    // from 이전 lookback까지 포함해야 하므로 to까지의 전체 캔들을 tradeDate 오름차순으로 가져와 롤링 계산한다.
    List<DailyCandleRow> candles = stockChartQueryDao.findCandles(stockCode, null, to);

    List<MovingAverageItem> dataPoints = new ArrayList<>();
    long windowSum = 0;
    for (int i = 0; i < candles.size(); i++) {
      windowSum += candles.get(i).closePrice();
      if (i >= window) {
        windowSum -= candles.get(i - window).closePrice();
      }
      if (i < window - 1) {
        continue; // 아직 N거래일치 데이터가 모이지 않음
      }
      LocalDate tradeDate = candles.get(i).tradeDate();
      if (tradeDate.isBefore(from) || tradeDate.isAfter(to)) {
        continue; // 요청 구간 밖 (lookback 용도)
      }
      dataPoints.add(new MovingAverageItem(tradeDate, windowSum / window));
    }
    return new MovingAverageResponse(period, dataPoints);
  }

  public BaseListResponse getBases(String stockCode, LocalDate from, LocalDate to) {
    requireStock(stockCode);
    List<BaseRow> bases = stockChartQueryDao.findBases(stockCode);

    List<BaseItem> items = new ArrayList<>();
    for (int i = 0; i < bases.size(); i++) {
      BaseRow base = bases.get(i);
      LocalDate startDate = base.startedAt();
      LocalDate endDate = null; // 가장 최신 베이스 = 진행 중
      if (i + 1 < bases.size()) {
        endDate = bases.get(i + 1).startedAt();
      }
      if (startDate.isAfter(to) || (endDate != null && endDate.isBefore(from))) {
        continue;
      }
      items.add(new BaseItem(startDate, endDate, base.supportPrice(), base.resistancePrice()));
    }
    return new BaseListResponse(items);
  }

  private void requireStock(String stockCode) {
    if (!stockChartQueryDao.existsStock(stockCode)) {
      throw new CoreException(ErrorType.NOT_FOUND, "종목을 찾을 수 없습니다: " + stockCode);
    }
  }
}
