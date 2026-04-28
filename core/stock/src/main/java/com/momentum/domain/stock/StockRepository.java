package com.momentum.domain.stock;

import java.util.Optional;

public interface StockRepository {
    Optional<Stock> findByStockCode(String stockCode);
    Stock save(Stock stock);
}
