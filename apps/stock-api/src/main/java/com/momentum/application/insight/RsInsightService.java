package com.momentum.application.insight;

import com.momentum.infrastructure.query.InsightQueryDao;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.RsResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RsInsightService {

  private final InsightQueryDao insightQueryDao;

  public RsResponse query(String stockCode, LocalDate at) {
    int rsScore = insightQueryDao.findLatestRsScore(stockCode)
        .orElseThrow(() -> new NoSuchElementException("RS 데이터가 없습니다: " + stockCode));

    BigDecimal rsValue = BigDecimal.valueOf(rsScore);
    return new RsResponse(rsValue, rsValue);
  }
}
