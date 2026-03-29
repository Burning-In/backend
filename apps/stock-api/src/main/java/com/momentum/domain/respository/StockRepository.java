package com.momentum.domain.respository;

import com.momentum.domain.entity.Stock;
import java.util.Optional;

public interface StockRepository {
    Optional<Stock> findByStockCode(String stockCode);
    Stock save(Stock stock);
}
