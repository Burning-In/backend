package com.momentum.domain.service.impl;

import com.momentum.domain.entity.indicator.StockBase;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class StockBaseServiceImpl {

  public StockBase createStockBase(StockBase stockBase) {
    return null;
  }

  public StockBase updateStockBase(StockBase stockBase) {
    return null;
  }

  public StockBase mergeStockBase(StockBase stockBase) {
    return null;
  }

  // merge작업 자체가...이전거를 삭제를 하면서,
  // 이전 저항선 내용들,,, 아니면 지지선 내용들을 통합해 주는거라서
  public StockBase deleteStockBase(StockBase stockBase) {
    return null;
  }
}
