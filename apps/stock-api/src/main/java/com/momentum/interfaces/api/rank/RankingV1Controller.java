package com.momentum.interfaces.api.rank;

import com.momentum.application.RankingService;
import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutReadyResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutSuccessResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ranking")
public class RankingV1Controller implements RankingV1ApiSpec {

  private final RankingService rankingService;

  @GetMapping("/breakout-success")
  @Override
  public ApiResponse<BreakoutSuccessResponse> getBreakoutSuccessRanking(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime at
  ) {
    return ApiResponse.success(rankingService.getBreakoutSuccessRanking(at));
  }

  @GetMapping("/breakout-ready")
  @Override
  public ApiResponse<BreakoutReadyResponse> getBreakoutReadyRanking(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime at
  ) {
    return ApiResponse.success(rankingService.getBreakoutReadyRanking(at));
  }
}
