package com.momentum.infrastructure.score;

import com.momentum.domain.score.StockRankScore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRankScoreJpaRepository extends JpaRepository<StockRankScore, Long> {

}
