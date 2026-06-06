package com.momentum.application.insight;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.VolumeResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VolumeInsightService {

  private final StockCandleRepository stockCandleRepository;
  private final StockBaseRepository stockBaseRepository;
  private final StockRepository stockRepository;

  public VolumeResponse query(String stockCode, LocalDate at) {
    Stock stock = findStock(stockCode);
    StockDailyCandle candle = stockCandleRepository.findRecentCandle(stock, at)
        .orElseThrow();
    long currentVolume = candle.getVolume();

    Optional<StockBase> baseOpt = stockBaseRepository.findCurrentBaseWithLines(stock);
    LocalDate baseFrom = baseOpt
        .map(b -> b.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
        .orElse(at.minusYears(1));

    Long baselineAvgVolume = stockCandleRepository.averageVolume(stock, baseFrom, at);
    if (baselineAvgVolume == null || baselineAvgVolume == 0) {
      baselineAvgVolume = currentVolume;
    }

    BigDecimal ratio = BigDecimal.valueOf(currentVolume)
        .divide(BigDecimal.valueOf(baselineAvgVolume), 4, RoundingMode.HALF_UP);

    return new VolumeResponse(baselineAvgVolume, currentVolume, ratio, null);
  }

  private Stock findStock(String stockCode) {
    return stockRepository.findByStockCode(stockCode)
        .orElseThrow(() -> new NoSuchElementException("종목을 찾을 수 없습니다: " + stockCode));
  }
}
