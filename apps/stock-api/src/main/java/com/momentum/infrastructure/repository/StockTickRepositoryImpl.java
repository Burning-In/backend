package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.StockTick;
import com.momentum.domain.respository.StockTickRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockTickRepositoryImpl implements StockTickRepository {

  private final StockTickJpaRepository stockTickJpaRepository;

  @Override
  public StockTick save(StockTick stockTick) {
    return stockTickJpaRepository.save(stockTick);
  }
}
