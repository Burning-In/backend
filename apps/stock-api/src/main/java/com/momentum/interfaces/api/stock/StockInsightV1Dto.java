package com.momentum.interfaces.api.stock;

import com.momentum.domain.stock.StockRegime;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class StockInsightV1Dto {

  // ===================== Regime (레짐) =====================

  public record StockRegimeResponse(
      StockRegime regime,
      Long currentPrice,
      Long supportLine,
      Long resistanceLine,
      BigDecimal changeRateFromReferenceLine
  ) {

  }

  // ===================== Moving Average (이동평균선) =====================

  public record MovingAverageResponse(
      Long currentPrice,
      Long ma50,
      Long ma150,
      Long ma200,
      boolean isAboveMa50,
      boolean isMa50AboveMa150,
      boolean isMa150AboveMa200
  ) {

  }

  // ===================== Base Stage (베이스 단계) =====================

  public record BaseStageResponse(
      Long stageLevel
  ) {

  }

  // ===================== Momentum (1년 모멘텀) =====================

  public record MomentumResponse(
      Long yearAgoPrice,
      LocalDate yearAgoDate,
      Long currentPrice,
      LocalDate currentDate,
      BigDecimal yearlyPriceChangeRate,
      BigDecimal percentileRank
  ) {

  }

  // ===================== FIP (흐름안정도) =====================

  public record FrogInPanResponse(
      int yearlyUpDays,
      int yearlyDownDays,
      BigDecimal fipScore,
      BigDecimal percentileRank
  ) {

  }

  // ===================== Volume (현재 거래량) =====================

  public record VolumeResponse(
      Long baselineAvgVolume,
      Long currentVolume,
      BigDecimal volumeToBaselineRatio,
      BigDecimal percentileRank
  ) {

  }

  // ===================== RS =====================

  public record RsResponse(
      BigDecimal rsValue,
      BigDecimal percentileRank
  ) {

  }

  // ===================== EPS =====================

  public record EpsResponse(
      List<QuarterlyEpsItem> quarterlyEps,
      BigDecimal changeRateYoY,
      BigDecimal percentileRank
  ) {

    public record QuarterlyEpsItem(
        String quarter,
        BigDecimal eps
    ) {

    }
  }
}
