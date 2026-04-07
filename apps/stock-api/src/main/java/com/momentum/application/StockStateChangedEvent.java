package com.momentum.application;

import com.momentum.domain.entity.StockState;

public record StockStateChangedEvent(
    String stockCode,
    StockState fromState,
    StockState toState
) {

}
