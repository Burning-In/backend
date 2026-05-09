package com.momentum.domain.rs;

import com.momentum.domain.stock.Stock;
import java.util.List;
import java.util.Optional;

public interface KOSPIRelativeStrengthRepository {

  List<KOPSIRelativeStrength> saveAll(List<KOPSIRelativeStrength> list);

  Optional<KOPSIRelativeStrength> findLatestByStock(Stock stock);
}
