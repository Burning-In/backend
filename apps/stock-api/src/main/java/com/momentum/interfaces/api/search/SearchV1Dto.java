package com.momentum.interfaces.api.search;

import java.math.BigDecimal;
import java.util.List;

public class SearchV1Dto {

    // ===================== Search Stock =====================

    public record StockSearchResponse(
        List<StockSearchItem> stocks
    ) {
        public record StockSearchItem(
            String stockCode,
            String stockName,
            BigDecimal price,
            String regime
        ) {}
    }
}
