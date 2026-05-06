package com.momentum.domain.rs;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RsTestSupport {

  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

  @Autowired
  private KOSPIRepository kospiRepository;
  @Autowired
  private StockCandleRepository stockCandleRepository;

  public void setupKospi(LocalDate today, long value) {
    kospiRepository.save(new KOSPI(value, today));
    kospiRepository.save(new KOSPI(value, today.minusMonths(3)));
    kospiRepository.save(new KOSPI(value, today.minusMonths(6)));
    kospiRepository.save(new KOSPI(value, today.minusMonths(9)));
    kospiRepository.save(new KOSPI(value, today.minusMonths(12)));
  }

  public void setupCandles(Stock stock, LocalDate today, long todayPrice, long threeMo, long sixMo, long nineMo, long twelveMo) {
    stockCandleRepository.save(
        StockDailyCandle.create(stock, today.format(FMT), todayPrice, todayPrice, todayPrice, todayPrice, 1000L, "2"));
    stockCandleRepository.save(
        StockDailyCandle.create(stock, today.minusMonths(3).format(FMT), threeMo, threeMo, threeMo, threeMo, 1000L, "2"));
    stockCandleRepository.save(
        StockDailyCandle.create(stock, today.minusMonths(6).format(FMT), sixMo, sixMo, sixMo, sixMo, 1000L, "2"));
    stockCandleRepository.save(
        StockDailyCandle.create(stock, today.minusMonths(9).format(FMT), nineMo, nineMo, nineMo, nineMo, 1000L, "2"));
    stockCandleRepository.save(
        StockDailyCandle.create(stock, today.minusMonths(12).format(FMT), twelveMo, twelveMo, twelveMo, twelveMo, 1000L, "2"));
  }
}
