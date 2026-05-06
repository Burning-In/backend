package com.momentum.domain.rs;

import com.momentum.domain.rs.KOSPIRawScoreCalculator.RSRawScore;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KOSPIRelativeStrengthService {

  private final KOSPIRepository kospiRepository;
  private final KOSPIRawScoreCalculator kOSPIRawScoreCalculator;
  private final StockRepository stockRepository;

  public List<KOPSIRelativeStrength> create(LocalDate today) {
    List<Stock> stocks = stockRepository.findAll();
    KOSPI kospi = kospiRepository.findByDate(today)
        .orElseThrow(IllegalStateException::new);
    List<RSRawScore> rawScores = kOSPIRawScoreCalculator.calculateScores(stocks, today);
    rawScores.sort((v1, v2) -> (int) (v1.rsRawScore() - v2.rsRawScore()));

    List<KOPSIRelativeStrength> relativeStrengths = new ArrayList<>();
    for (int i = 0; i < rawScores.size(); i++) {
      double ratio = (double) i / rawScores.size();
      int rsRating = (int) Math.round(ratio * 98) + 1;
      KOPSIRelativeStrength relativeStrength = new KOPSIRelativeStrength(rsRating, rawScores.get(i).stockCode(), kospi);
      relativeStrengths.add(relativeStrength);
    }

    return relativeStrengths;
  }
}
