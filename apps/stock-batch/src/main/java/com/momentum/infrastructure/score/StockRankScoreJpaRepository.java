package com.momentum.infrastructure.score;

import com.momentum.domain.score.StockRankScore;
import com.momentum.domain.stock.Stock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRankScoreJpaRepository extends JpaRepository<StockRankScore, Long> {

  @Query("SELECT s FROM StockRankScore s WHERE s.stock = :stock AND s.deletedAt IS NULL ORDER BY s.baseDate DESC LIMIT 1")
  Optional<StockRankScore> findLatestByStock(@Param("stock") Stock stock);

  List<StockRankScore> findAllByBaseDateAndDeletedAtIsNull(LocalDate baseDate);
}
