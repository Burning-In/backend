package com.momentum.infrastructure.query;

import java.time.LocalDate;

public record DailyCandleRow(LocalDate tradeDate, long openPrice, long highPrice, long lowPrice, long closePrice,
                             long volume) {

}
