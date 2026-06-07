package com.momentum.interfaces.api.rank;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutReadyResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;

@Tag(name = "Ranking V1 API", description = "레짐별 랭킹 API 입니다. 돌파 성공/돌파준비 2개 레짐에 대해서만 랭킹을 제공합니다.")
public interface RankingV1ApiSpec {
  // ===================== HTTP (스냅샷) =====================

  @Operation(
      summary = "돌파 성공 랭킹 스냅샷 조회",
      description = """
          BREAKOUT_SUCCESS 레짐 종목을 모멘텀(1차 필터) → FIP(2차 필터) 기준으로 정렬한 상위 50개를 반환합니다.
          초기 페이지 로딩 시 호출하여 현재 스냅샷을 수신하고, 실시간 변경분은 SSE로 구독하세요.
          현재가는 조회 시점(at) 기준 가장 최근 체결가(틱)로 채워집니다.
          """
  )
  ApiResponse<BreakoutSuccessResponse> getBreakoutSuccessRanking(
      @Schema(description = "조회 시점") LocalDateTime at
  );

  @Operation(
      summary = "돌파준비 랭킹 스냅샷 조회",
      description = """
          BREAKOUT_READY 레짐 종목을 모멘텀(1차 필터) → FIP(2차 필터) 기준으로 정렬한 상위 50개를 반환합니다.
          초기 페이지 로딩 시 호출하여 현재 스냅샷을 수신하고, 실시간 변경분은 SSE로 구독하세요.
          현재가는 조회 시점(at) 기준 가장 최근 체결가(틱)로 채워집니다.
          """
  )
  ApiResponse<BreakoutReadyResponse> getBreakoutReadyRanking(
      @Schema(description = "조회 시점") LocalDateTime at
  );
}
