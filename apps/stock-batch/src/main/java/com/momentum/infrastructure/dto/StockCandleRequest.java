package com.momentum.infrastructure.dto;

public record StockCandleRequest(
    String stockCode,
    int count,
    String startDate,
    String endDate
) {

}
