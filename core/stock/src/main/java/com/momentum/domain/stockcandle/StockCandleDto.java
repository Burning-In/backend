package com.momentum.domain.stockcandle;

public record StockCandleDto(
    String date,
    long openPrice,
    long highPrice,
    long lowPrice,
    long closePrice,
    long volume,
    long tradingValue,
    String priceChangeSign
) {

}
