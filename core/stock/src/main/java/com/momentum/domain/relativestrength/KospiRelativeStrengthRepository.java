package com.momentum.domain.relativestrength;

import com.momentum.domain.stock.Stock;
import java.util.List;
import java.util.Optional;

public interface KospiRelativeStrengthRepository {

  List<KospiRelativeStrength> saveAll(List<KospiRelativeStrength> list);

  Optional<KospiRelativeStrength> findLatestByStock(Stock stock);
}
