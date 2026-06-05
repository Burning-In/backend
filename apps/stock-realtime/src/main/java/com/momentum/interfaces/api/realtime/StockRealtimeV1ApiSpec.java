package com.momentum.interfaces.api.realtime;

import com.momentum.application.dto.TickResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Stock Realtime V1 API", description = "종목 실시간 틱 데이터 SSE 스트림 API 입니다.")
public interface StockRealtimeV1ApiSpec {

  @Operation(
      summary = "실시간 틱 데이터 구독",
      description = """
          종목의 실시간 체결 틱 데이터를 SSE(Server-Sent Events)로 스트리밍합니다.

          - SSE는 변경 이벤트 전달 전용입니다. 연결 이전의 현재 상태(스냅샷)는 보장하지 않습니다.
          - 초기 페이지 로딩 시에는 HTTP 스냅샷 API를 먼저 호출하고, 이후 SSE로 변경분을 수신하세요.
          - 웹소켓(LS증권)에서 틱 데이터가 수신되지 않으면 SSE 이벤트도 발생하지 않습니다 (장마감/장일시정지 구간).
          - 장 재개 시 서버가 자동으로 웹소켓을 재연결하며, SSE 스트림은 유지됩니다.

          event: tick
          data: { TickResponse JSON }
          """,
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "SSE 스트림 연결 성공. text/event-stream으로 틱 이벤트를 수신합니다.",
              content = @Content(
                  mediaType = "text/event-stream",
                  schema = @Schema(implementation = TickResponse.class)
              )
          )
      }
  )
  SseEmitter subscribeTick(
      @Schema(description = "종목 코드") String stockCode
  );
}
