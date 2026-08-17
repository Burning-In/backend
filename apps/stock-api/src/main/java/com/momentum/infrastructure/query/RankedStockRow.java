package com.momentum.infrastructure.query;

import java.math.BigDecimal;

public record RankedStockRow(String stockName, String stockCode, BigDecimal momentum, BigDecimal fip) {

}
