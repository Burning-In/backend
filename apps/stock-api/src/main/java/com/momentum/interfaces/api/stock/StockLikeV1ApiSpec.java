package com.momentum.interfaces.api.stock;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockLikeV1Dto.LikeStockResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Stock Like V1 API", description = "관심 종목 관련 API 입니다.")
public interface StockLikeV1ApiSpec {

  @Operation(
      summary = "즐겨찾기 추가",
      description = "종목을 즐겨찾기(관심 종목)에 추가합니다."
  )
  @Parameter(name = "Authorization", in = ParameterIn.HEADER, description = "Bearer <accessToken>", required = true)
  ApiResponse<Void> addLike(
      @Schema(description = "종목 코드") String stockCode
  );

  @Operation(
      summary = "즐겨찾기 해제",
      description = "종목을 즐겨찾기(관심 종목)에서 제거합니다."
  )
  @Parameter(name = "Authorization", in = ParameterIn.HEADER, description = "Bearer <accessToken>", required = true)
  ApiResponse<Void> removeLike(
      @Schema(description = "종목 코드") String stockCode
  );

  @Operation(
      summary = "관심 종목 리스트 조회",
      description = "사이드바에 표시할 관심 종목 리스트를 조회합니다."
  )
  @Parameter(name = "Authorization", in = ParameterIn.HEADER, description = "Bearer <accessToken>", required = true)
  ApiResponse<LikeStockResponse> getLikeStocks();
}
