package com.momentum.domain.stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {

  Optional<Stock> findByStockCode(String stockCode);

  Optional<Stock> findById(Long id);

  Stock save(Stock stock);

  List<Stock> saveAll(List<Stock> stocks);

  List<Stock> search(String query);

  List<Stock> findAll();
}
