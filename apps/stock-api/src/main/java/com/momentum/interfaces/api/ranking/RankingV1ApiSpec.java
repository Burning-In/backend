package com.momentum.interfaces.api.ranking;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.ranking.RankingV1Dto.FallReadyResponse;
import com.momentum.interfaces.api.ranking.RankingV1Dto.FallStartResponse;
import com.momentum.interfaces.api.ranking.RankingV1Dto.RiseReadyResponse;
import com.momentum.interfaces.api.ranking.RankingV1Dto.RiseStartResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Ranking V1 API", description = "랭킹 페이지 관련 API 입니다.")
public interface RankingV1ApiSpec {

    @Operation(
        summary = "상승시작 랭킹 조회",
        description = "저항선 돌파 이후 상승을 시작한 종목 랭킹을 조회합니다."
    )
    ApiResponse<RiseStartResponse> getRiseStartRanking();

    @Operation(
        summary = "상승준비 랭킹 조회",
        description = "저항선 돌파를 준비 중인 종목 랭킹을 조회합니다."
    )
    ApiResponse<RiseReadyResponse> getRiseReadyRanking();

    @Operation(
        summary = "하락시작 랭킹 조회",
        description = "저항선 돌파 실패 이후 하락을 시작한 종목 랭킹을 조회합니다."
    )
    ApiResponse<FallStartResponse> getFallStartRanking();

    @Operation(
        summary = "하락준비 랭킹 조회",
        description = "저지선 붕괴 이후 하락을 준비 중인 종목 랭킹을 조회합니다."
    )
    ApiResponse<FallReadyResponse> getFallReadyRanking();

}
