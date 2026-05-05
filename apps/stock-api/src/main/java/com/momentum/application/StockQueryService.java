package com.momentum.application;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockQueryService {

  private final StockRepository stockRepository;

  @Transactional
  public List<Stock> search(String query) {
    return stockRepository.search(query);
  }
}
