package com.momentum.domain.pricepoint.service;

import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stock.Stock;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockPricePointTypeResolver {

  private static final double FLAT_THRESHOLD = 1.0;

  private final StockPricePointRepository stockPricePointRepository;

  @Transactional
  public List<StockPricePoint> resolveType(Stock stock) {
    List<StockPricePoint> recent3 = stockPricePointRepository.findTop3ByStockOrderByCreatedAtDesc(stock.getId());
    if (recent3.size() < 3) {
      throw new IllegalArgumentException();
    }

    StockPricePoint p3 = recent3.get(0);
    StockPricePoint p2 = recent3.get(1);
    StockPricePoint p1 = recent3.get(2);

    if (isFlat(p1.getPrice(), p2.getPrice())) {
      List<StockPricePoint> recent4 = stockPricePointRepository.findTop4ByStockOrderByCreatedAtDesc(stock.getId());
      if (recent4.size() < 4) {
        StockPricePointType newType = StockPricePointType.resolve(null, p2.getPrice(), p3.getPrice());
        return updateType(newType, p1, p2);
      }
      StockPricePoint p0 = recent4.get(3);
      StockPricePointType newType = StockPricePointType.resolve(p0.getPrice(), p2.getPrice(), p3.getPrice());
      return updateType(newType, p1, p2);
    }

    StockPricePointType newType = StockPricePointType.resolve(p1.getPrice(), p2.getPrice(), p3.getPrice());
    p2.updateType(newType);
    return stockPricePointRepository.saveAll(List.of(p2));
  }

  private List<StockPricePoint> updateType(StockPricePointType type, StockPricePoint p1, StockPricePoint p2) {
    p1.updateType(type);
    p2.updateType(type);
    return stockPricePointRepository.saveAll(List.of(p1, p2));
  }

  public boolean isFlat(long price1, long price2) {
    double diff = Math.abs(price1 - price2) / (double) price2 * 100;
    return diff <= FLAT_THRESHOLD;
  }
}
