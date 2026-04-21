package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.stock.Stock;
import com.momentum.domain.respository.StockRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StockRepositoryImpl implements StockRepository {

  private final StockJpaRepository stockCandleRepository;

  @Override
  public Optional<Stock> findByStockCode(String stockCode) {
    return stockCandleRepository.findStockByCode(stockCode);
  }

  @Override
  public Stock save(Stock stock) {
    return stockCandleRepository.save(stock);
  }
}
