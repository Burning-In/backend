package com.momentum.domain.service.impl;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.respository.StockCandleRepository;
import com.momentum.domain.respository.StockRepository;
import com.momentum.domain.service.StockCandleService;
import com.momentum.infrastructure.api.dto.StockChartInfoResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockCandleServiceImpl implements StockCandleService {

  private final StockRepository stockRepository;
  private final StockCandleRepository stockCandleRepository;

  @Override
  @Transactional
  public List<StockCandle> create(String stockCode, StockChartInfoResponse stockChartInfoResponse) {
    Stock stock = stockRepository.findByStockCode(stockCode)
        .orElseThrow(IllegalArgumentException::new);

    List<StockCandle> dailyCandles = stockChartInfoResponse.candleResponses()
        .stream()
        .map(candle -> StockCandle.daily(stock, candle))
        .toList();

    return stockCandleRepository.save(dailyCandles);
  }
}
