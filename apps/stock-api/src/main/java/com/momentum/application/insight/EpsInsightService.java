package com.momentum.application.insight;

import com.momentum.domain.eps.StockEps;
import com.momentum.domain.eps.StockEpsRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.EpsResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.EpsResponse.QuarterlyEpsItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EpsInsightService {

  private static final int RECENT_QUARTERS = 5;

  private final StockEpsRepository stockEpsRepository;
  private final StockRepository stockRepository;

  public EpsResponse query(String stockCode, LocalDate at) {
    Stock stock = findStock(stockCode);
    List<StockEps> epsList = stockEpsRepository.findRecentByStock(stock, RECENT_QUARTERS);

    List<QuarterlyEpsItem> items = epsList.stream()
        .map(e -> new QuarterlyEpsItem(
            e.getQuarter().toString(),
            BigDecimal.valueOf(e.getEps()).setScale(2, RoundingMode.HALF_UP)
        ))
        .toList();

    BigDecimal changeRateYoY = null;
    if (!epsList.isEmpty()) {
      StockEps latest = epsList.get(0);
      if (latest.getYearOverYearChangeRate() != null) {
        changeRateYoY = BigDecimal.valueOf(latest.getYearOverYearChangeRate()).setScale(4, RoundingMode.HALF_UP);
      }
    }

    return new EpsResponse(items, changeRateYoY, null);
  }

  private Stock findStock(String stockCode) {
    return stockRepository.findByStockCode(stockCode)
        .orElseThrow(() -> new NoSuchElementException("종목을 찾을 수 없습니다: " + stockCode));
  }
}
