package com.momentum.domain.base.service;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.base.entity.StockBaseLine;
import com.momentum.domain.base.entity.StockBaseLineType;
import org.springframework.stereotype.Component;

@Component
public class StockBaseLineTypeConvertor {
  ///  추후 % 반영필요
  // 새 베이스 생성 시점에 이전 베이스의 라인 타입 전환
  // 저항선 → 지지선: 새 베이스의 지지선 가격 이하인 저항선 (저항선을 넘어 새 베이스가 형성됨)
  // 지지선 → 저항선: 새 베이스의 저항선 가격 이상인 지지선 (지지선이 무너져 새 저항선이 됨)
  public void convertLineType(StockBase previousBase, StockBase newBase) {
    if (previousBase == null) {
      return;
    }
    for (StockBaseLine line : previousBase.getStockBaseLines()) {
      if (line.getLineType() == StockBaseLineType.RESISTANCE
          && line.getPrice() <= newBase.getLowestSupportLinePrice()) {
        line.convertLineType();
      }
      if (line.getLineType() == StockBaseLineType.SUPPORT
          && line.getPrice() >= newBase.getHighestResistancePrice()) {
        line.convertLineType();
      }
    }
  }

}
