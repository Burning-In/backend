package com.momentum.domain.relativestrength;

import com.momentum.domain.relativestrength.KospiRawScoreCalculator.RSRawScore;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KospiRelativeStrengthService {

  private final KospiRepository kospiRepository;
  private final KospiRawScoreCalculator kospiRawScoreCalculator;
  private final StockRepository stockRepository;
  private final KospiRelativeStrengthRepository kospiRelativeStrengthRepository;

  public List<KospiRelativeStrength> create(LocalDate today) {
    List<Stock> stocks = stockRepository.findAll();
    Kospi kospi = kospiRepository.findByDate(today)
        .orElseThrow(IllegalStateException::new);
    List<RSRawScore> rawScores = kospiRawScoreCalculator.calculateScores(stocks, today);
    rawScores.sort((v1, v2) -> (int) (v1.rsRawScore() - v2.rsRawScore()));

    List<KospiRelativeStrength> relativeStrengths = new ArrayList<>();
    for (int i = 0; i < rawScores.size(); i++) {
      double ratio = (double) i / rawScores.size();
      int rsRating = (int) Math.round(ratio * 98) + 1;
      KospiRelativeStrength relativeStrength = new KospiRelativeStrength(rsRating, rawScores.get(i).stockCode(), kospi);
      relativeStrengths.add(relativeStrength);
    }

    return kospiRelativeStrengthRepository.saveAll(relativeStrengths);
  }
}
