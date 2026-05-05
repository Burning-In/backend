package com.momentum.interfaces.api.stock;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockDailyInfoV1Dto.DailyCandleResponse;
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
      @Schema(description = "종료일(분)") LocalDateTime to
  );
}
