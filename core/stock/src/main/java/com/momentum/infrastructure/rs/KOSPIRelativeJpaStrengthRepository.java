package com.momentum.infrastructure.rs;

import com.momentum.domain.rs.KOPSIRelativeStrength;
import com.momentum.domain.stock.Stock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KOSPIRelativeJpaStrengthRepository extends JpaRepository<KOPSIRelativeStrength, Long> {

  @Query("SELECT k FROM KOPSIRelativeStrength k WHERE k.stock = :stock AND k.deletedAt IS NULL ORDER BY k.createdAt DESC LIMIT 1")
  Optional<KOPSIRelativeStrength> findLatestByStock(@Param("stock") Stock stock);
}
