package com.momentum.interfaces.api.stock;

import java.util.List;

public class StockLikeV1Dto {

  public record LikeStockResponse(
      List<LikeStockItem> stocks
  ) {

    public record LikeStockItem(
        String stockCode,
        String stockName
    ) {

    }
  }
}
