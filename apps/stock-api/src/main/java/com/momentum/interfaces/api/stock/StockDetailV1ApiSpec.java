package com.momentum.interfaces.api.stock;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockDetailV1Dto.CandleChartResponse;
import com.momentum.interfaces.api.stock.StockDetailV1Dto.EpsResponse;
import com.momentum.interfaces.api.stock.StockDetailV1Dto.ExpectedReturnResponse;
import com.momentum.interfaces.api.stock.StockDetailV1Dto.InsightResponse;
import com.momentum.interfaces.api.stock.StockDetailV1Dto.PbResponse;
import com.momentum.interfaces.api.stock.StockDetailV1Dto.SummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Stock Detail V1 API", description = "종목 상세 페이지 관련 API 입니다.")
public interface StockDetailV1ApiSpec {

    @Operation(
        summary = "종목 요약 정보 조회",
        description = "종목 이름, 섹터, 상태, 가격, 기타노이즈를 조회합니다."
    )
    ApiResponse<SummaryResponse> getSummary(
        @Schema(description = "종목 코드") String stockCode
    );

    @Operation(
        summary = "캔들 차트 데이터 조회",
        description = "종목의 캔들 차트 데이터를 타임라인 기준으로 조회합니다."
    )
    ApiResponse<CandleChartResponse> getCandleChart(
        @Schema(description = "종목 코드") String stockCode,
        @Schema(description = "타임라인 (예: DAY, WEEK, MONTH)") String timeline
    );

    @Operation(
        summary = "인사이트 조회",
        description = "레짐 상태/설명 (Core Insight) 및 이동평균선/베이스/저항선/지지선 정보 (Technical Insight)를 조회합니다."
    )
    ApiResponse<InsightResponse> getInsight(
        @Schema(description = "종목 코드") String stockCode
    );

    @Operation(
        summary = "RS 정보 조회",
        description = "KOSPI 대비 RS 정보를 조회합니다."
    )
    ApiResponse<PbResponse> getPb(
        @Schema(description = "종목 코드") String stockCode
    );

    @Operation(
        summary = "EPS 정보 조회",
        description = "분기별 EPS 및 EPS 평가 정보를 조회합니다."
    )
    ApiResponse<EpsResponse> getEps(
        @Schema(description = "종목 코드") String stockCode
    );

    @Operation(
        summary = "기대 수익률 조회",
        description = "종목의 기대 수익률 값 및 모델 설명을 조회합니다."
    )
    ApiResponse<ExpectedReturnResponse> getExpectedReturn(
        @Schema(description = "종목 코드") String stockCode
    );

    @Operation(
        summary = "즐겨찾기 추가",
        description = "종목을 즐겨찾기(관심 종목)에 추가합니다."
    )
    ApiResponse<Void> addLike(
        @Schema(description = "종목 코드") String stockCode
    );

    @Operation(
        summary = "즐겨찾기 해제",
        description = "종목을 즐겨찾기(관심 종목)에서 제거합니다."
    )
    ApiResponse<Void> removeLike(
        @Schema(description = "종목 코드") String stockCode
    );

}
