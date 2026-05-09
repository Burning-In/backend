package com.momentum.application.insight;

import com.momentum.domain.score.FipScore;
import com.momentum.domain.score.StockRankScore;
import com.momentum.domain.score.StockRankScoreRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.FrogInPanResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FrogInPanInsightService {

  private final StockRankScoreRepository stockRankScoreRepository;

  public FrogInPanResponse query(Stock stock, LocalDate at) {
    StockRankScore rankScore = stockRankScoreRepository.findLatestByStock(stock)
        .orElseThrow(() -> new NoSuchElementException("FIP 데이터가 없습니다: " + stock.getCode()));

    FipScore fipScore = rankScore.getFipScore();
    BigDecimal percentileRank = computePercentile(rankScore);

    return new FrogInPanResponse(fipScore.getUpDays(), fipScore.getDownDays(), fipScore.getFip(), percentileRank);
  }

  private BigDecimal computePercentile(StockRankScore myScore) {
    List<StockRankScore> allScores = stockRankScoreRepository.findAllByBaseDate(myScore.getBaseDate());
    List<BigDecimal> nonNull = allScores.stream()
        .map(s -> s.getFipScore().getFip())
        .filter(Objects::nonNull)
        .toList();
    if (nonNull.isEmpty()) {
      return null;
    }
    BigDecimal myFip = myScore.getFipScore().getFip();
    long below = nonNull.stream().filter(v -> v.compareTo(myFip) < 0).count();
    return BigDecimal.valueOf((double) below / nonNull.size() * 100).setScale(1, RoundingMode.HALF_UP);
  }
}
