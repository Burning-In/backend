package com.momentum.interfaces.api.stock;

import com.momentum.sharedkernel.StockRegime;
import com.momentum.sharedkernel.StockTrend;
import com.momentum.infrastructure.query.StockMetaRow;

public class StockMetaV1Dto {

  public record StockMetaResponse(
      String stockCode,
      String stockName,
      StockRegime regime,
      StockTrend trend
  ) {

    public static StockMetaResponse from(StockMetaRow stock) {
      return new StockMetaResponse(
          stock.stockCode(),
          stock.stockName(),
          StockRegime.valueOf(stock.stockRegime()),
          StockTrend.valueOf(stock.stockTrend()));
    }
  }
}
