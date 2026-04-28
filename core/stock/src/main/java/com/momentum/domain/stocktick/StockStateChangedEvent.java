package com.momentum.domain.stocktick;


import com.momentum.domain.stock.StockRegime;

public record StockStateChangedEvent(
    String stockCode,
    StockRegime fromState,
    StockRegime toState
) {

}
