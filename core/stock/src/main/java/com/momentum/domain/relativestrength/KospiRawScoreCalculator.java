package com.momentum.domain.relativestrength;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockCandleRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KospiRawScoreCalculator {

  private static final int CURRENT_MONTH = 0;
  private static final int THREE_MONTHS_AGO = 3;
  private static final int SIX_MONTHS_AGO = 6;
  private static final int NINE_MONTHS_AGO = 9;
  private static final int TWELVE_MONTHS_AGO = 12;

  private static final double PERCENT_SCALE = 100;

  private final StockCandleRepository stockCandleRepository;
  private final KospiRepository kospiRepository;

  public List<RSRawScore> calculateScores(List<Stock> stocks, LocalDate today, double recentQuarterWeight,
      double halfYearWeight, double threeQuartersWeight, double fullYearWeight) {
    if (stocks.isEmpty()) {
      return List.of();
    }
    List<RSRawScore> rawScores = new ArrayList<>();
    for (Stock stock : stocks) {
      double stockScore = calculateStockScore(stock, today, recentQuarterWeight, halfYearWeight, threeQuartersWeight,
          fullYearWeight);
      double indexScore = calculateIndexScore(today, recentQuarterWeight, halfYearWeight, threeQuartersWeight,
          fullYearWeight);
      double rsRawScore = (stockScore / indexScore) * PERCENT_SCALE;
      rawScores.add(new RSRawScore(stock, rsRawScore));
    }

    return rawScores;
  }

  private double calculateStockScore(Stock stock, LocalDate today, double recentQuarterWeight, double halfYearWeight,
      double threeQuartersWeight, double fullYearWeight) {
    double todayPrice = getCandleFrom(CURRENT_MONTH, stock, today);
    double threeMonthPrice = getCandleFrom(THREE_MONTHS_AGO, stock, today);
    double sixMonthPrice = getCandleFrom(SIX_MONTHS_AGO, stock, today);
    double nineMonthPrice = getCandleFrom(NINE_MONTHS_AGO, stock, today);
    double twelveMonthPrice = getCandleFrom(TWELVE_MONTHS_AGO, stock, today);

    return calculateWeightedScore(todayPrice, threeMonthPrice, sixMonthPrice, nineMonthPrice, twelveMonthPrice,
        recentQuarterWeight, halfYearWeight, threeQuartersWeight, fullYearWeight);
  }

  private double calculateIndexScore(LocalDate today, double recentQuarterWeight, double halfYearWeight,
      double threeQuartersWeight, double fullYearWeight) {
    double todayPrice = getIndexFrom(CURRENT_MONTH, today);
    double threeMonthPrice = getIndexFrom(THREE_MONTHS_AGO, today);
    double sixMonthPrice = getIndexFrom(SIX_MONTHS_AGO, today);
    double nineMonthPrice = getIndexFrom(NINE_MONTHS_AGO, today);
    double twelveMonthPrice = getIndexFrom(TWELVE_MONTHS_AGO, today);

    return calculateWeightedScore(todayPrice, threeMonthPrice, sixMonthPrice, nineMonthPrice, twelveMonthPrice,
        recentQuarterWeight, halfYearWeight, threeQuartersWeight, fullYearWeight);
  }

  private double calculateWeightedScore(double todayPrice, double threeMonthPrice, double sixMonthPrice,
      double nineMonthPrice, double twelveMonthPrice, double recentQuarterWeight, double halfYearWeight,
      double threeQuartersWeight, double fullYearWeight) {
    return (recentQuarterWeight * todayPrice / threeMonthPrice) + (halfYearWeight * todayPrice / sixMonthPrice)
        + (threeQuartersWeight * todayPrice / nineMonthPrice) + (fullYearWeight * todayPrice / twelveMonthPrice);
  }

  private double getIndexFrom(int month, LocalDate today) {
    LocalDate monthAgo = today.minusMonths(month);
    return kospiRepository.findRecentKospi(monthAgo)
        .orElseThrow(IllegalArgumentException::new)
        .getValue();
  }

  private double getCandleFrom(int month, Stock stock, LocalDate today) {
    LocalDate monthAgo = today.minusMonths(month);
    return stockCandleRepository.findRecentCandle(stock, monthAgo)
        .orElseThrow(IllegalArgumentException::new)
        .getClosePrice();
  }
}
