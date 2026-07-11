package com.momentum.application;

import com.momentum.domain.eps.StockEpsInfo;
import com.momentum.domain.eps.StockEpsService;
import com.momentum.domain.stock.Stock;
import com.momentum.infrastructure.EpsProvider;
import com.momentum.infrastructure.dto.FinancialRatioResponse.Output;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockEpsFacade {

  private final EpsProvider epsProvider;
  private final StockEpsService stockEpsService;

  public void record(Stock stock) {
    List<Output> quarterlyEps = epsProvider.getQuarterlyEps(stock.getName());
    stockEpsService.create(fromResponse(stock, quarterlyEps));
  }

  private List<StockEpsInfo> fromResponse(Stock stock, List<Output> quarterlyEps) {
    return quarterlyEps.stream()
        .map(quarter -> StockEpsInfo.of(stock, quarter.stacYymm(), quarter.eps()))
        .toList();
  }
}
