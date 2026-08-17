package com.momentum.infrastructure.stock;

import static com.momentum.domain.stock.QStock.stock;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StockRepositoryImpl implements StockRepository {

  private final StockJpaRepository stockCandleRepository;
  private final JPAQueryFactory queryFactory;

  @Override
  public Optional<Stock> findByStockCode(String stockCode) {
    return stockCandleRepository.findStockByCode(stockCode);
  }

  @Override
  public Stock save(Stock stock) {
    return stockCandleRepository.save(stock);
  }

  @Override
  public List<Stock> saveAll(List<Stock> stocks) {
    return stockCandleRepository.saveAll(stocks);
  }

  @Override
  public List<Stock> findAll() {
    return stockCandleRepository.findAll();
  }
}
