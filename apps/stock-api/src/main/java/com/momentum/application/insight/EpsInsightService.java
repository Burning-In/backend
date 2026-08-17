package com.momentum.application.insight;

import com.momentum.infrastructure.query.InsightQueryDao;
import com.momentum.infrastructure.query.InsightRows.EpsRow;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.EpsResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.EpsResponse.QuarterlyEpsItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EpsInsightService {

  private static final int RECENT_QUARTERS = 5;

  private final InsightQueryDao insightQueryDao;

  public EpsResponse query(String stockCode, LocalDate at) {
    List<EpsRow> rows = insightQueryDao.findRecentEps(stockCode, RECENT_QUARTERS);

    List<QuarterlyEpsItem> items = rows.stream()
        .map(row -> new QuarterlyEpsItem(
            YearMonth.from(row.quarter()).toString(),
            BigDecimal.valueOf(row.eps()).setScale(2, RoundingMode.HALF_UP)))
        .toList();

    return new EpsResponse(items, changeRateYoY(rows), null);
  }

  private BigDecimal changeRateYoY(List<EpsRow> rows) {
    if (rows.isEmpty()) {
      return null;
    }
    Double latestChangeRate = rows.getFirst().yearOverYearChangeRate();
    if (latestChangeRate == null) {
      return null;
    }
    return BigDecimal.valueOf(latestChangeRate).setScale(4, RoundingMode.HALF_UP);
  }
}
