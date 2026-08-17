package com.momentum.domain.relativestrength;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class KospiRelativeStrengthService {

  private static final double RECENT_QUARTER_WEIGHT = 0.4;
  private static final double HALF_YEAR_WEIGHT = 0.2;
  private static final double THREE_QUARTERS_WEIGHT = 0.2;
  private static final double FULL_YEAR_WEIGHT = 0.2;

  private static final int UPTREND_RS_THRESHOLD = 80;

  private final KospiRepository kospiRepository;
  private final StockRepository stockRepository;
  private final KospiRelativeStrengthRepository kospiRelativeStrengthRepository;
  private final KospiRawScoreCalculator kospiRawScoreCalculator;

  @Transactional
  public List<KospiRelativeStrength> create(LocalDate today) {
    Kospi kospi = kospiRepository.findByDate(today)
        .orElseThrow(IllegalStateException::new);
    List<RSRawScore> rawScores = calculateRawScoresInAscendingOrder(today);

    List<KospiRelativeStrength> relativeStrengths = new ArrayList<>();
    for (int rank = 0; rank < rawScores.size(); rank++) {
      Stock stock = rawScores.get(rank).stock();
      KospiRelativeStrength relativeStrength = KospiRelativeStrength.create(rank, rawScores.size(), stock, kospi);
      stock.updateTrend(relativeStrength.getRsScore(), UPTREND_RS_THRESHOLD);
      stockRepository.save(stock);
      relativeStrengths.add(relativeStrength);
    }

    return kospiRelativeStrengthRepository.saveAll(relativeStrengths);
  }

  // RS 등급은 원점수 순위의 백분위이므로, 등급을 매기기 전에 원점수 오름차순 정렬이 반드시 필요하다.
  private List<RSRawScore> calculateRawScoresInAscendingOrder(LocalDate today) {
    List<Stock> stocks = stockRepository.findAll();
    return kospiRawScoreCalculator.calculateScores(stocks, today,
            RECENT_QUARTER_WEIGHT, HALF_YEAR_WEIGHT, THREE_QUARTERS_WEIGHT, FULL_YEAR_WEIGHT)
        .stream()
        .sorted(Comparator.comparingDouble(RSRawScore::rsRawScore))
        .toList();
  }
}
