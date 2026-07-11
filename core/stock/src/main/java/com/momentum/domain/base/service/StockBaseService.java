package com.momentum.domain.base.service;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseService {

  public static final double BASE_BOUNDARY_THRESHOLD = 5.0;

  private final StockBaseInitializer stockBaseInitializer;
  private final StockBaseConfirmer stockBaseConfirmer;
  private final StockBasePointIntegrator stockBasePointIntegrator;
  private final StockBaseStageLevelAdjuster stockBaseStageLevelAdjuster;
  private final StockBaseLineTypeConvertor stockBaseLineTypeConvertor;

  private final StockBaseRepository stockBaseRepository;

  public void resolve(List<StockPricePoint> typeConfirmedPoints) {
    if (typeConfirmedPoints == null || typeConfirmedPoints.isEmpty()) {
      throw new IllegalArgumentException("stockPricePoint cannot be null");
    }
    StockPricePoint confirmedPricePoint = typeConfirmedPoints.getFirst();
    if (StockPricePointType.isNonPivot(confirmedPricePoint)) {// HighOrLow로 명확하게 해야함
      return;
    }
    Optional<StockBase> currentBaseOpt = stockBaseRepository.findCurrentBaseWithLines(confirmedPricePoint.getStock());
    if (currentBaseOpt.isEmpty()) {
      stockBaseInitializer.resolve(confirmedPricePoint);
      return;
    }

    StockBase currentBase = currentBaseOpt.get();
    confirmStockBase(confirmedPricePoint, currentBase);
    // if문이 여기에 있었으면 좋겠는데, 어떤 조건에서 이게 들어가는지 모르겠네
    stockBasePointIntegrator.resolve(confirmedPricePoint, currentBase, BASE_BOUNDARY_THRESHOLD);
    // 애도 그러고.. 어떤 조건에서 이게 있는거지?,, 근데 솔직히 몰라도 되긴한데 ㅋㅋ
    stockBaseStageLevelAdjuster.resolve(confirmedPricePoint, currentBase, BASE_BOUNDARY_THRESHOLD);
  }

  // 새로운거 만드는 건데 이게 맞나?, 살짝 어렵게 해놓았네
  private void confirmStockBase(StockPricePoint confirmedPricePoint, StockBase currentBase) {
    StockBase newBase = stockBaseConfirmer.resolve(confirmedPricePoint, currentBase, BASE_BOUNDARY_THRESHOLD);
    stockBaseLineTypeConvertor.convertLineType(newBase);
  }
}
