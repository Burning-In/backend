package com.momentum.application;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import com.momentum.support.error.CoreException;
import com.momentum.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockQueryService {

  private final StockRepository stockRepository;

  @Transactional(readOnly = true)
  public List<Stock> search(String query) {
    return stockRepository.search(query);
  }

  @Transactional(readOnly = true)
  public Stock getByCode(String stockCode) {
    return stockRepository.findByStockCode(stockCode)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "종목을 찾을 수 없습니다: " + stockCode));
  }
}
