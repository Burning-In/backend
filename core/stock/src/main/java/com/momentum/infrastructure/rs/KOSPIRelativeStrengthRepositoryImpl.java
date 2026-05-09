package com.momentum.infrastructure.rs;

import com.momentum.domain.rs.KOPSIRelativeStrength;
import com.momentum.domain.rs.KOSPIRelativeStrengthRepository;
import com.momentum.domain.stock.Stock;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KOSPIRelativeStrengthRepositoryImpl implements KOSPIRelativeStrengthRepository {

  private final KOSPIRelativeJpaStrengthRepository kospiRelativeJpaStrengthRepository;

  @Override
  public List<KOPSIRelativeStrength> saveAll(List<KOPSIRelativeStrength> relativeStrengths) {
    return kospiRelativeJpaStrengthRepository.saveAll(relativeStrengths);
  }

  @Override
  public Optional<KOPSIRelativeStrength> findLatestByStock(Stock stock) {
    return kospiRelativeJpaStrengthRepository.findLatestByStock(stock);
  }
}
