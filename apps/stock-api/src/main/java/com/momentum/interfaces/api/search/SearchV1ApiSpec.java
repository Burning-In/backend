package com.momentum.interfaces.api.search;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.search.SearchV1Dto.StockSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Search V1 API", description = "검색 오버레이 관련 API 입니다.")
public interface SearchV1ApiSpec {

    @Operation(
        summary = "종목 검색",
        description = "종목 이름으로 검색하여 이름 / 가격 / 레짐 정보를 반환합니다. 결과가 없으면 빈 리스트를 반환합니다."
    )
    ApiResponse<StockSearchResponse> searchStocks(
        @Schema(name = "query", description = "검색할 종목 이름")
        String query
    );
}
