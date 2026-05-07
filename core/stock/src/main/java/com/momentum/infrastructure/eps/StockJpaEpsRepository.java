package com.momentum.infrastructure.eps;

import com.momentum.domain.eps.StockEps;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockJpaEpsRepository extends JpaRepository<StockEps, Long> {

}
