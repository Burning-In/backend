package com.momentum.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseRepositoryImpl {
  private final StockBaseJpaRepository stockBaseJpaRepository;

}
