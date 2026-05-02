package com.momentum.domain.pricepoint.service;

import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.infrastructure.pricepoint.dto.RecentPricePoints;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockPricePointTypeNormalResolver {

  private final StockPricePointRepository stockPricePointRepository;

  public List<StockPricePoint> resolve(RecentPricePoints points) {
    StockPricePointType newType = StockPricePointType.resolve(
        points.point1(),
        points.point2(),
        points.point3()
    );
    points.point2().updateType(newType);
    return stockPricePointRepository.saveAll(List.of(points.point2()));
  }
}
