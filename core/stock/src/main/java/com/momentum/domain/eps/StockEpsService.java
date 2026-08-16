package com.momentum.domain.eps;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockEpsService {

  private final StockEpsRepository stockEpsRepository;

  public List<StockEps> create(List<StockEpsInfo> stockEpsInfos) {
    List<StockEps> result = new ArrayList<>();
    for (StockEpsInfo stockEpsInfo : stockEpsInfos) {
      Double yearOverYearChangeRate = calculateYearOverYearChangeRate(stockEpsInfo, stockEpsInfo.eps());
      StockEps stockEps = new StockEps(stockEpsInfo.eps(), stockEpsInfo.quarter(), yearOverYearChangeRate, stockEpsInfo.stock());
      result.add(stockEps);
    }

    return stockEpsRepository.saveAll(result);
  }

  private Double calculateYearOverYearChangeRate(StockEpsInfo stockEpsInfo, double currentEps) {
    StockEps oneYearAgoEps = getOneYearAgoEps(stockEpsInfo);
    if (!canCompareWith(oneYearAgoEps)) {
      return null;
    }
    double baseEps = oneYearAgoEps.getEps();
    return (currentEps - baseEps) / Math.abs(baseEps);
  }

  private boolean canCompareWith(StockEps oneYearAgoEps) {
    return oneYearAgoEps != null && oneYearAgoEps.getEps() != 0;
  }

  private StockEps getOneYearAgoEps(StockEpsInfo stockEpsInfo) {
    return stockEpsRepository.findOneYearAgo(stockEpsInfo.stock(), stockEpsInfo.quarter())
        .orElse(null);
  }
}
