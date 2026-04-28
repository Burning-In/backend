package com.momentum.infrastructure.stock;

import com.momentum.domain.stock.Stock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {

  Optional<Stock> findStockByCode(String code);
}
