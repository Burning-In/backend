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
      StockEps oneYearAgoEps = getOneYearAgo(stockEpsInfo);
      Double yoy = calculateYoY(oneYearAgoEps, stockEpsInfo.eps());
      StockEps stockEps = new StockEps(stockEpsInfo.eps(), stockEpsInfo.quarterlyDate(), yoy, stockEpsInfo.stock());
      result.add(stockEps);
    }

    return stockEpsRepository.saveAll(result);
  }

  private Double calculateYoY(StockEps oneYearAgoEps, double currentEps) {
    if (oneYearAgoEps == null) {
      return null;
    }
    return (currentEps - oneYearAgoEps.getEps()) / oneYearAgoEps.getEps();
  }

  private StockEps getOneYearAgo(StockEpsInfo stockEpsInfo) {
    return stockEpsRepository.findOneYearAgo(stockEpsInfo.stock(), stockEpsInfo.quarterlyDate())
        .orElse(null);
  }
}
