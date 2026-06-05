package com.momentum.domain.stock;

public record StockStateChangedEvent(
    String stockCode,
    StockRegime fromState,
    StockRegime toState
) {

}
