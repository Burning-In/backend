package com.momentum.interfaces.api.stock;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockChartV1Dto.BaseListResponse;
import com.momentum.interfaces.api.stock.StockChartV1Dto.DailyCandleResponse;
import com.momentum.sharedkernel.StockMovingAveragePeriod;
import com.momentum.interfaces.api.stock.StockChartV1Dto.MovingAverageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface StockChartV1ApiSpec {

  @Operation(
      summary = "일봉 데이터 조회",
      description = "종목의 일봉 데이터를 조회합니다."
  )
  ApiResponse<DailyCandleResponse> getDailyCandle(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "시작일") LocalDate from,
      @Schema(description = "종료일(분)") LocalDateTime to
  );

  @Operation(
      summary = "이동평균선 데이터 조회",
      description = "차트에 표시할 MA50·MA150·MA200 일별 값을 조회합니다. 백엔드에서 from 기준 최대 200일 이전 데이터까지 참조해 계산합니다."
  )
  ApiResponse<MovingAverageResponse> getMovingAverages(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "이동평균 기간") StockMovingAveragePeriod period,
      @Schema(description = "시작일") LocalDate from,
      @Schema(description = "종료일(분)") LocalDateTime to
  );

  @Operation(
      summary = "베이스 목록 조회",
      description = "차트에 표시할 베이스(지지/저항 구간) 목록을 조회합니다. endDate가 null이면 현재 진행 중인 베이스입니다."
  )
  ApiResponse<BaseListResponse> getBases(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "시작일") LocalDate from,
      @Schema(description = "종료일(분)") LocalDateTime to
  );
}
