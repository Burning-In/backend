package com.momentum.interfaces.api.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/realtime/ranking")
public class RealtimeRankingV1Controller implements RealtimeRankingV1ApiSpec {

    @GetMapping(value = "/breakout-start/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public SseEmitter subscribeBreakoutStartRanking() {
        // TODO: RankingFacade SSE 등록
        return new SseEmitter();
    }

    @GetMapping(value = "/breakout-ready/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public SseEmitter subscribeBreakoutReadyRanking() {
        // TODO: RankingFacade SSE 등록
        return new SseEmitter();
    }

    @GetMapping(value = "/breakout-failed/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public SseEmitter subscribeBreakoutFailedRanking() {
        // TODO: RankingFacade SSE 등록
        return new SseEmitter();
    }
}
