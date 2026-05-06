package com.momentum.infrastructure.rs;

import com.momentum.domain.rs.KOPSIRelativeStrength;
import com.momentum.domain.rs.KOSPIRelativeStrengthRepository;
import java.util.List;
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
}
