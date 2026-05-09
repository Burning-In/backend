package com.momentum.application.insight;

import com.momentum.domain.eps.StockEps;
import com.momentum.domain.eps.StockEpsRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.EpsResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.EpsResponse.QuarterlyEpsItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EpsInsightService {

  private static final int RECENT_QUARTERS = 5;

  private final StockEpsRepository stockEpsRepository;

  public EpsResponse query(Stock stock, LocalDate at) {
    List<StockEps> epsList = stockEpsRepository.findRecentByStock(stock, RECENT_QUARTERS);

    List<QuarterlyEpsItem> items = epsList.stream()
        .map(e -> new QuarterlyEpsItem(
            e.getQuarterlyDate().toString(),
            BigDecimal.valueOf(e.getEps()).setScale(2, RoundingMode.HALF_UP)
        ))
        .toList();

    BigDecimal changeRateYoY = null;
    if (!epsList.isEmpty()) {
      StockEps latest = epsList.get(0);
      if (latest.getYearOverYear() != null) {
        changeRateYoY = BigDecimal.valueOf(latest.getYearOverYear()).setScale(4, RoundingMode.HALF_UP);
      }
    }

    return new EpsResponse(items, changeRateYoY, null);
  }
}
