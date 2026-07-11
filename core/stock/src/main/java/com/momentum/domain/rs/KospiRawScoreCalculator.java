package com.momentum.domain.rs;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KospiRawScoreCalculator {

  private final StockCandleRepository stockCandleRepository;
  private final KospiRepository kospiRepository;

  public List<RSRawScore> calculateScores(List<Stock> stocks, LocalDate today) {
    List<RSRawScore> rawScores = new ArrayList<>();
    for (Stock stock : stocks) {
      double rsRawScore = (calculateStockScore(stock, today) / calculateIndexScore(stock, today)) * 100;
      rawScores.add(new RSRawScore(stock, rsRawScore));
    }

    return rawScores;
  }

  private double calculateStockScore(Stock stock, LocalDate today) {
    double todayPrice = getCandleFrom(0, stock, today).getClosePrice();
    double threeMonthPrice = getCandleFrom(3, stock, today).getClosePrice();
    double sixMonthPrice = getCandleFrom(6, stock, today).getClosePrice();
    double nineMonthPrice = getCandleFrom(9, stock, today).getClosePrice();
    double twelveMonthPrice = getCandleFrom(12, stock, today).getClosePrice();

    return calculateWeightedScore(todayPrice, threeMonthPrice, sixMonthPrice, nineMonthPrice, twelveMonthPrice);
  }

  private double calculateIndexScore(Stock stock, LocalDate today) {
    double todayPrice = getIndexFrom(0, stock, today).getValue();
    double threeMonthPrice = getIndexFrom(3, stock, today).getValue();
    double sixMonthPrice = getIndexFrom(6, stock, today).getValue();
    double nineMonthPrice = getIndexFrom(9, stock, today).getValue();
    double twelveMonthPrice = getIndexFrom(12, stock, today).getValue();

    return calculateWeightedScore(todayPrice, threeMonthPrice, sixMonthPrice, nineMonthPrice, twelveMonthPrice);
  }

  private double calculateWeightedScore(double todayPrice, double threeMonthPrice, double sixMonthPrice, double nineMonthPrice,
      double twelveMonthPrice) {
    double q1 = todayPrice / threeMonthPrice;
    double q2 = todayPrice / sixMonthPrice;
    double q3 = todayPrice / nineMonthPrice;
    double q4 = todayPrice / twelveMonthPrice;

    return (0.4 * q1) + (0.2 * q2) + (0.2 * q3) + (0.2 * q4);
  }


  private Kospi getIndexFrom(int month, Stock stock, LocalDate today) {
    LocalDate monthAgo = today.minusMonths(month);
    return kospiRepository.findRecentKospi(monthAgo)
        .orElseThrow(IllegalArgumentException::new);
  }

  private StockDailyCandle getCandleFrom(int month, Stock stock, LocalDate today) {
    LocalDate monthAgo = today.minusMonths(month);
    return stockCandleRepository.findRecentCandle(stock, monthAgo)
        .orElseThrow(IllegalArgumentException::new);
  }

  public record RSRawScore(Stock stockCode, Double rsRawScore) {

  }
}


