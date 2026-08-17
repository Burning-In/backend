package com.momentum.application.insight;

import com.momentum.infrastructure.query.InsightQueryDao;
import com.momentum.infrastructure.query.InsightRows.RankScoreRow;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.FrogInPanResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FrogInPanInsightService {

  private final InsightQueryDao insightQueryDao;

  public FrogInPanResponse query(String stockCode, LocalDate at) {
    RankScoreRow row = insightQueryDao.findLatestRankScore(stockCode)
        .orElseThrow(() -> new NoSuchElementException("FIP 데이터가 없습니다: " + stockCode));

    List<BigDecimal> peerScores = insightQueryDao.findFipScores(row.baseDate());
    BigDecimal percentileRank = PercentileRank.of(row.fip(), peerScores);

    return new FrogInPanResponse(row.upDays(), row.downDays(), row.fip(), percentileRank);
  }
}
