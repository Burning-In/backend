package com.momentum.batch.job.pricepoint;

import com.momentum.domain.base.service.StockBaseService;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.service.StockPricePointService;
import com.momentum.domain.pricepoint.service.typedecider.StockPricePointTypeDecider;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class StockPricePointItemProcessor implements ItemProcessor<Stock, Stock> {

  private final StockPricePointService stockPricePointService;
  private final StockBaseService stockBaseService;
  private final StockPricePointTypeDecider stockPricePointTypeDecider;
  private final StockCandleRepository stockCandleRepository;

  @Value("#{jobParameters['baseDate']}")
  private String baseDateStr;

  @Override
  public Stock process(Stock stock) throws Exception {
    LocalDate baseDate = LocalDate.parse(baseDateStr);
    StockDailyCandle candle = stockCandleRepository
        .findLastCandleBeforeDate(stock, baseDate)
        .orElseThrow(() -> new IllegalStateException("캔들 데이터 없음: " + stock.getId()));
    /// 도메인서비스나 어플리케이션 서비스로 묶을 필요가 있지않나
    StockPricePoint stockPricePoint = stockPricePointService.resolvePricePoint(candle);
    if (stockPricePoint == null) { // null주는지 체크 필요
      return null;
    }
    List<StockPricePoint> typeConfirmedPoints = stockPricePointTypeDecider.resolvePointTypes(stock);
    stockBaseService.resolve(typeConfirmedPoints);
    ///
    return stock;
  }
}
