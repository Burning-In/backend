package com.momentum.application.insight;

import com.momentum.infrastructure.query.MovingAverageQueryDao;
import com.momentum.infrastructure.query.MovingAverageRow;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.MovingAverageResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovingAverageInsightService {

  private final MovingAverageQueryDao movingAverageQueryDao;

  public MovingAverageResponse query(String stockCode, LocalDate at) {
    MovingAverageRow row = movingAverageQueryDao.findByStockCode(stockCode, at);

    return new MovingAverageResponse(
        row.currentPrice(),
        row.ma50(),
        row.ma150(),
        row.ma200(),
        isAbove(row.currentPrice(), row.ma50()),
        isAbove(row.ma50(), row.ma150()),
        isAbove(row.ma150(), row.ma200())
    );
  }

  private boolean isAbove(Long upper, Long lower) {
    if (upper == null || lower == null) {
      return false;
    }
    return upper > lower;
  }
}
