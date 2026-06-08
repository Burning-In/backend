package com.momentum.interfaces.api.stock;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockMetaV1Dto.StockMetaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Stock Meta V1 API", description = "종목 메타데이터(이름/코드/레짐/추세) 조회 API")
public interface StockMetaV1ApiSpec {

  @Operation(
      summary = "종목 메타 조회",
      description = "종목코드로 종목명·현재 레짐·추세 등 메타데이터를 조회합니다."
  )
  ApiResponse<StockMetaResponse> getStockMeta(
      @Parameter(description = "종목코드", example = "000660") String stockCode
  );
}
