package com.momentum.application.dto;

import com.momentum.domain.entity.StockRegime;

public record StockStateChangedEvent(
    String stockCode,
    StockRegime fromState,
    StockRegime toState
) {

}
