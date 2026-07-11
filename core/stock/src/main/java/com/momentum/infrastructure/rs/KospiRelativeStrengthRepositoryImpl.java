package com.momentum.infrastructure.rs;

import com.momentum.domain.rs.KospiRelativeStrength;
import com.momentum.domain.rs.KospiRelativeStrengthRepository;
import com.momentum.domain.stock.Stock;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KospiRelativeStrengthRepositoryImpl implements KospiRelativeStrengthRepository {

  private final KospiRelativeJpaStrengthRepository kospiRelativeJpaStrengthRepository;

  @Override
  public List<KospiRelativeStrength> saveAll(List<KospiRelativeStrength> relativeStrengths) {
    return kospiRelativeJpaStrengthRepository.saveAll(relativeStrengths);
  }

  @Override
  public Optional<KospiRelativeStrength> findLatestByStock(Stock stock) {
    return kospiRelativeJpaStrengthRepository.findLatestByStock(stock);
  }
}
