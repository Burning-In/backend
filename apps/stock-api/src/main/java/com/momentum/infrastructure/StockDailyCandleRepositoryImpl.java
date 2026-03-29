package com.momentum.infrastructure;

import com.momentum.domain.respository.StockDailyCandleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StockDailyCandleRepositoryImpl implements StockDailyCandleRepository {
    private final StockDailyCandleJpaRepository stockDailyCandleJpaRepository;

}
