package com.momentum.interfaces.api.ranking;

import com.momentum.application.dto.ranking.RealtimeBreakoutReadyItem;
import com.momentum.application.dto.ranking.RealtimeBreakoutSuccessItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Realtime Ranking V1 API", description = "레짐별 실시간 랭킹 SSE API 입니다. 돌파 성공/돌파준비 2개 레짐에 대해서만 제공됩니다. 기본 경로: /api/v1/realtime/ranking")
public interface RealtimeRankingV1ApiSpec {

  @Operation(
      summary = "돌파 성공 랭킹 변경 구독",
      description = """
          BREAKOUT_SUCCESS 레짐의 랭킹 변경을 SSE로 수신합니다.
          종목의 레짐 상태가 변경될 때 해당 레짐의 랭킹 구성이 재산출되어 이벤트가 발생합니다.

          event: ranking-update
          data: [ RealtimeBreakoutSuccessItem JSON ]
          """,
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "SSE 스트림 연결 성공.",
              content = @Content(
                  mediaType = "text/event-stream",
                  array = @ArraySchema(schema = @Schema(implementation = RealtimeBreakoutSuccessItem.class))
              )
          )
      }
  )
  SseEmitter subscribeBreakoutSuccessRanking();

  @Operation(
      summary = "돌파준비 랭킹 변경 구독",
      description = """
          BREAKOUT_READY 레짐의 랭킹 변경을 SSE로 수신합니다.
          종목의 레짐 상태가 변경될 때 해당 레짐의 랭킹 구성이 재산출되어 이벤트가 발생합니다.

          event: ranking-update
          data: [ RealtimeBreakoutReadyItem JSON ]
          """,
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "SSE 스트림 연결 성공.",
              content = @Content(
                  mediaType = "text/event-stream",
                  array = @ArraySchema(schema = @Schema(implementation = RealtimeBreakoutReadyItem.class))
              )
          )
      }
  )
  SseEmitter subscribeBreakoutReadyRanking();
}
