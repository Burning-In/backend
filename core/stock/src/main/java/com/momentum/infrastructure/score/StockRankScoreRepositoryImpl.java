package com.momentum.infrastructure.score;

import com.momentum.domain.score.StockRankScore;
import com.momentum.domain.score.StockRankScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockRankScoreRepositoryImpl implements StockRankScoreRepository {

  private final StockRankScoreJpaRepository stockRankScoreJpaRepository;


  @Override
  public StockRankScore save(StockRankScore stockRankScore) {
    return stockRankScoreJpaRepository.save(stockRankScore);
  }
}
