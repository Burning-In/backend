package com.momentum.interfaces.api.stock;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.BaseStageResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.EpsResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.FrogInPanResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.MomentumResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.MovingAverageResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.RsResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.StockRegimeResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.VolumeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;

@Tag(name = "Stock Insight V1 API", description = "종목 지표별 인사이트 관련 API 입니다.")
public interface StockInsightV1ApiSpec {

  @Operation(
      summary = "베이스 단계 조회",
      description = """
          현재 종목의 베이스 단계(stageLevel)를 조회합니다. 베이스가 없으면 stageLevel은 null입니다.

          - 1~2단계: "초기 상승 흐름이 예상됩니다." | subDescription: "초기 상승 구간, 매수 적합"
          - 3단계 이상: "강한 시세 분출이 예상됩니다." | subDescription: "시세 분출 구간, 매도 준비 필요"
          - 베이스 없음(null): "현재 베이스가 형성되지 않았습니다." | subDescription: null
          """
  )
  ApiResponse<BaseStageResponse> getBaseStage(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "조회 시점") LocalDateTime at
  );

  @Operation(
      summary = "레짐 조회",
      description = """
          현재 종목의 레짐 및 지지선/저항선 정보를 조회합니다. 공통 수치: 지지선, 저항선, 현재가(당일 가격).

          레짐별 코멘트 및 수치 기준:
          - 돌파시작: "베이스의 저항선을 뚫고 상승하기 시작한 상태입니다." | 오늘가격 > 저항선 > 지지선 | 저항선 대비 오늘가격(+)
          - 돌파준비(저항선근접): "베이스 내에서 돌파 준비 중인 상태입니다." | 저항선 > 오늘가격 > 지지선 | 저항선 대비 오늘가격(-)
          - 돌파준비(VCP): "변동성이 축소되며 돌파를 준비 중인 상태입니다." | 저항선 > 오늘가격 > 지지선 | 저항선 대비 오늘가격(-)
          - 돌파실패: "저항선 돌파에 실패하고 베이스 내에서 횡보 중인 상태입니다." | 저항선 > 오늘가격 > 지지선 | 저항선 대비 오늘가격(-)
          - 하방이탈: "베이스의 지지선 아래로 이탈한 상태입니다." | 저항선 > 지지선 > 오늘가격 | 지지선 대비 오늘가격(-)
          - 방향미정: "아직 베이스가 형성되지 않아 방향을 판단하기 어려운 상태입니다." | 수치 : null
          """
  )
  ApiResponse<StockRegimeResponse> getRegime(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "조회 시점") LocalDateTime at
  );

  @Operation(
      summary = "이동평균선 조회",
      description = """
          50일/150일/200일 이동평균선과 현재가를 비교합니다. 정배열 조건: 현재가 > MA50 > MA150 > MA200.

          - 정배열(isAligned=true): "상승의 흐름을 뒷받침하는 정배열입니다."
          - 역배열(isAligned=false): "이평선이 역배열 상태로, 상승 흐름이 약화되어 있습니다."
          """
  )
  ApiResponse<MovingAverageResponse> getMovingAverage(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "조회 시점") LocalDateTime at
  );

  @Operation(
      summary = "1년 모멘텀 조회",
      description = """
          1년간 주식 가격 상승폭 및 상위 백분위를 조회합니다.

          - 50% 이상 또는 시장 상위 30%: "지난 1년간 강한 가격 상승이 나타났습니다."
          - 20% 이상 ~ 50% 미만: "완만한 상승 흐름을 보입니다."
          - 20% 미만: "뚜렷한 상승 모멘텀이 부족합니다."
          """
  )
  ApiResponse<MomentumResponse> getMomentum(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "조회 시점") LocalDateTime at
  );

  @Operation(
      summary = "흐름안정도(FIP) 조회",
      description = """
          1년간 상승/하락 일수 기반의 모멘텀 흐름 안정도를 조회합니다.

          - 상승일 비중 > 55%: "1년간, 상승일 비중이 높아 상승 흐름이 비교적 일관됩니다."
          - 45% ~ 55%: "상승 흐름은 있으나 일관성은 다소 부족합니다."
          - 45% 미만: "상승 흐름의 일관성이 부족한 구간입니다."
          """
  )
  ApiResponse<FrogInPanResponse> getFrogInPan(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "조회 시점") LocalDateTime at
  );

  @Operation(
      summary = "현재 거래량 조회",
      description = """
          베이스라인 평균 거래량 대비 현재 거래량을 조회합니다.
          베이스가 존재하는 레짐(4종)에서만 데이터가 제공되며, 방향미정 레짐인 경우 null을 반환합니다.

          - 현재 거래량 >= 베이스 평균: "거래량이 가격 움직임을 뒷받침합니다."
          - 현재 거래량 < 베이스 평균: "가격 움직임 대비 거래량이 부족합니다."
          """
  )
  ApiResponse<VolumeResponse> getVolume(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "조회 시점") LocalDateTime at
  );

  @Operation(
      summary = "RS 정보 조회",
      description = "KOSPI 대비 RS 값 및 상위 백분위를 조회합니다."
  )
  ApiResponse<RsResponse> getRs(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "조회 시점") LocalDateTime at
  );

  @Operation(
      summary = "EPS 정보 조회",
      description = """
          전년 동분기 대비 EPS 증감률 및 상위 백분위를 조회합니다. (Q1 → 전년 Q1, Q2 → 전년 Q2 비교)

          - +20% 이상: "EPS가 강한 상승세를 보이고 있습니다."
          - 0% ~ +20%: "EPS가 완만한 상승세를 보이고 있습니다."
          - 음수: "EPS가 감소세를 보이고 있습니다."
          """
  )
  ApiResponse<EpsResponse> getEps(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "조회 시점") LocalDateTime at
  );
}
