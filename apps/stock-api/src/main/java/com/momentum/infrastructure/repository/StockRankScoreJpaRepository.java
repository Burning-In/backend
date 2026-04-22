package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.score.StockRankScore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRankScoreJpaRepository extends JpaRepository<StockRankScore, Long> {

}
