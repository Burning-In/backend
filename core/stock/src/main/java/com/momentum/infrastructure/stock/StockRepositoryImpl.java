package com.momentum.infrastructure.stock;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
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
