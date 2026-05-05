package com.momentum.interfaces.api.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/stocks/{stockCode}/realtime")
public class StockRealtimeV1Controller implements StockRealtimeV1ApiSpec {

  @GetMapping(value = "/tick", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Override
  public SseEmitter subscribeTick(
      @PathVariable String stockCode
  ) {
    // TODO: SseEmitter 생성 후 StockRealtimeFacade에 등록
    return new SseEmitter();
  }
}
