package com.momentum.application;

public record StockCandleRequest(
    String stockCode,
    int count,
    String startDate,
    String endDate
) {

}
