package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.respository.StockCandleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StockCandleRepositoryImpl implements StockCandleRepository {

  private final StockCandleJpaRepository stockCandleJpaRepository;

  @Override
  public List<StockCandle> save(List<StockCandle> dailyCandles) {
    return stockCandleJpaRepository.saveAll(dailyCandles);
  }
}
