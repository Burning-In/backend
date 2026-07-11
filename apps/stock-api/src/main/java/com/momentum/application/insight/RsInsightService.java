package com.momentum.application.insight;

import com.momentum.domain.rs.KospiRelativeStrength;
import com.momentum.domain.rs.KospiRelativeStrengthRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.RsResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RsInsightService {

  private final KospiRelativeStrengthRepository kospiRelativeStrengthRepository;
  private final StockRepository stockRepository;

  public RsResponse query(String stockCode, LocalDate at) {
    Stock stock = findStock(stockCode);
    KospiRelativeStrength rs = kospiRelativeStrengthRepository.findLatestByStock(stock)
        .orElseThrow(() -> new NoSuchElementException("RS 데이터가 없습니다: " + stock.getCode()));

    BigDecimal rsValue = BigDecimal.valueOf(rs.getRsScore());
    return new RsResponse(rsValue, rsValue);
  }

  private Stock findStock(String stockCode) {
    return stockRepository.findByStockCode(stockCode)
        .orElseThrow(() -> new NoSuchElementException("종목을 찾을 수 없습니다: " + stockCode));
  }
}
