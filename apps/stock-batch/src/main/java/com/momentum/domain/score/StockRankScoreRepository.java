package com.momentum.domain.score;

import com.momentum.domain.stock.Stock;
import com.momentum.sharedkernel.StockRegime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockRankScoreRepository {

  StockRankScore save(StockRankScore stockRankScore);

  Optional<StockRankScore> findLatestByStock(Stock stock);

  List<StockRankScore> findAllByBaseDate(LocalDate baseDate);
}
