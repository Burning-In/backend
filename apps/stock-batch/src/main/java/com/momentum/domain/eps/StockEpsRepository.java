package com.momentum.domain.eps;

import com.momentum.domain.stock.Stock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockEpsRepository {

  Optional<StockEps> findOneYearAgo(Stock stock, LocalDate quarter);

  List<StockEps> saveAll(List<StockEps> stockEps);
}
