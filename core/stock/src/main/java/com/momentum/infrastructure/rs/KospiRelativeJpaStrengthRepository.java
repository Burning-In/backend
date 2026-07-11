package com.momentum.infrastructure.rs;

import com.momentum.domain.rs.KospiRelativeStrength;
import com.momentum.domain.stock.Stock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KospiRelativeJpaStrengthRepository extends JpaRepository<KospiRelativeStrength, Long> {

  @Query("SELECT k FROM KospiRelativeStrength k WHERE k.stock = :stock AND k.deletedAt IS NULL ORDER BY k.createdAt DESC LIMIT 1")
  Optional<KospiRelativeStrength> findLatestByStock(@Param("stock") Stock stock);
}
