package com.momentum.interfaces.api.stock;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockDailyInfoV1Dto.DailyCandleResponse;
import com.momentum.interfaces.api.stock.StockDailyInfoV1Dto.IntradayDailyCandleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface StockDailyInfoV1ApiSpec {

  @Operation(
      summary = "일봉 데이터 조회",
      description = "종목의 일봉 데이터를 조회합니다."
  )
  ApiResponse<DailyCandleResponse> getDailyCandle(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "시작일") LocalDate from,
      @Schema(description = "종료일") LocalDate to
  );

  @Operation(
      summary = "장중 일봉 조회",
      description = "특정 종목의 해당 날짜에 대해, 지정한 시점까지 누적된 일봉(시가, 고가, 저가, 현재가, 거래량)을 조회합니다."
  )
  ApiResponse<IntradayDailyCandleResponse> getIntradayDailyCandle(
      @Schema(description = "종목 코드") String stockCode,
      @Schema(description = "해당일") LocalDate tradeDate,
      @Schema(description = "해당 시간(분)") LocalDateTime to
  );

}
