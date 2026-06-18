package com.momentum.interfaces.api.stock;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockTrend;

public class StockMetaV1Dto {

  public record StockMetaResponse(
      String stockCode,
      String stockName,
      StockRegime regime,
      StockTrend trend
  ) {

    public static StockMetaResponse from(Stock stock) {
      return new StockMetaResponse(
          stock.getCode(),
          stock.getName(),
          stock.getStockRegime(),
          stock.getStockTrend());
    }
  }
}
