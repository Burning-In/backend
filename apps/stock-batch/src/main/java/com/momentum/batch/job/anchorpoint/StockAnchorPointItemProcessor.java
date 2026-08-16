package com.momentum.batch.job.anchorpoint;

import com.momentum.domain.base.service.StockBaseService;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.service.StockAnchorPointService;
import com.momentum.domain.anchorpoint.service.typedecider.StockAnchorPointTypeDecider;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.time.LocalDate;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class StockAnchorPointItemProcessor implements ItemProcessor<Stock, Stock> {

  private final StockAnchorPointService stockAnchorPointService;
  private final StockBaseService stockBaseService;
  private final StockAnchorPointTypeDecider stockAnchorPointTypeDecider;
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
    StockAnchorPoint stockAnchorPoint = stockAnchorPointService.resolveAnchorPoint(candle);
    if (stockAnchorPoint == null) { // null주는지 체크 필요
      return null;
    }
    stockAnchorPointTypeDecider.resolvePointTypes(stock)
        .stream()
        .max(Comparator.comparing(StockAnchorPoint::getTradeDate))
        .ifPresent(stockBaseService::resolve);
    ///
    return stock;
  }
}
