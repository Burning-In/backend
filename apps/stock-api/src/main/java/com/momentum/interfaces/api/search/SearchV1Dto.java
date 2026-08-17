package com.momentum.interfaces.api.search;

import com.momentum.sharedkernel.StockRegime;
import com.momentum.infrastructure.query.StockMetaRow;
import java.math.BigDecimal;
import java.util.List;

public class SearchV1Dto {

  // ===================== Search Stock =====================

  public record StockSearchResponse(
      List<StockSearchItem> stocks
  ) {

    public record StockSearchItem(
        String stockCode,
        String stockName,
        BigDecimal price,
        StockRegime regime
    ) {

    }

    public static StockSearchResponse from(List<StockMetaRow> stocks, List<BigDecimal> prices) {
      List<StockSearchItem> items = stocks.stream()
          .map(stock -> new StockSearchItem(
              stock.stockCode(),
              stock.stockName(),
              null,
              StockRegime.valueOf(stock.stockRegime())))
          .toList();

      return new StockSearchResponse(items);
    }
  }
}
