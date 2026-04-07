package com.momentum.application.dto;

public record StockCandleRequest(
    String stockCode,
    int count,
    String startDate,
    String endDate
) {

}
