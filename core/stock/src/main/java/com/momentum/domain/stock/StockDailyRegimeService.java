package com.momentum.domain.stock;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockDailyRegimeService {

  private static final double BREAKOUT_THRESHOLD = 3.0;
  private static final double LINE_APPROACH_THRESHOLD = 3.0;

  private final StockRepository stockRepository;
  private final StockBaseRepository stockBaseRepository;
  private final StockPricePointRepository stockPricePointRepository;

  public void finalizeDailyState(StockDailyCandle stockDailyCandle) {
    Stock stock = stockDailyCandle.getStock();

    Optional<StockBase> currentBaseOpt = stockBaseRepository.findCurrentBaseWithLines(stock);
    Optional<StockPricePoint> recentPricePointOpt = stockPricePointRepository.findLatestByStock(stock);
    if (currentBaseOpt.isEmpty() || recentPricePointOpt.isEmpty()) {
      stock.update(StockRegime.DIRECTION_UNDETERMINED);
      stockRepository.save(stock);
      return;
    }

    StockRegime newRegime = StockRegime.determineDailyRegime(stock,
        stockDailyCandle.getClosePrice(),
        recentPricePointOpt.get(), currentBaseOpt.get(), BREAKOUT_THRESHOLD, LINE_APPROACH_THRESHOLD);
    stock.update(newRegime);
    stockRepository.save(stock);
  }
}
