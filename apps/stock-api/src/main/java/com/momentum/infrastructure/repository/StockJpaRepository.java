package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.Stock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {

  Optional<Stock> findStockByCode(String code);
}
