package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.score.StockRankScore;
import com.momentum.domain.respository.StockRankScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockRankScoreJpaRepositoryImpl implements StockRankScoreRepository {

  private final StockRankScoreJpaRepository stockRankScoreJpaRepository;


  @Override
  public StockRankScore save(StockRankScore stockRankScore) {
    return stockRankScoreJpaRepository.save(stockRankScore);
  }
}
