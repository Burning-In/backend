package com.momentum.application.insight;

import com.momentum.infrastructure.query.InsightQueryDao;
import com.momentum.infrastructure.query.InsightRows.VolumeRow;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.VolumeResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VolumeInsightService {

  private static final int BASELINE_YEARS_WITHOUT_BASE = 1;

  private final InsightQueryDao insightQueryDao;

  public VolumeResponse query(String stockCode, LocalDate at) {
    VolumeRow row = insightQueryDao.findVolume(stockCode, at)
        .orElseThrow(() -> new NoSuchElementException("종목을 찾을 수 없습니다: " + stockCode));

    long currentVolume = row.currentVolume();
    LocalDate baselineFrom = baselineFrom(row, at);
    long baselineAvgVolume = baselineAvgVolume(stockCode, baselineFrom, at, currentVolume);

    BigDecimal ratio = BigDecimal.valueOf(currentVolume)
        .divide(BigDecimal.valueOf(baselineAvgVolume), 4, RoundingMode.HALF_UP);

    return new VolumeResponse(baselineAvgVolume, currentVolume, ratio, null);
  }

  private LocalDate baselineFrom(VolumeRow row, LocalDate at) {
    if (row.baselineFrom() == null) {
      return at.minusYears(BASELINE_YEARS_WITHOUT_BASE);
    }
    return row.baselineFrom();
  }

  private long baselineAvgVolume(String stockCode, LocalDate from, LocalDate to, long fallback) {
    Long averageVolume = insightQueryDao.averageVolume(stockCode, from, to);
    if (averageVolume == null || averageVolume == 0) {
      return fallback;
    }
    return averageVolume;
  }
}
