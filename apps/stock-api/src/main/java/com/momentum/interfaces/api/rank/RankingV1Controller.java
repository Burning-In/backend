package com.momentum.interfaces.api.rank;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutFailedResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutReadyResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutStartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ranking")
public class RankingV1Controller implements RankingV1ApiSpec {

  @GetMapping("/breakout-start")
  @Override
  public ApiResponse<BreakoutStartResponse> getBreakoutStartRanking() {
    // TODO: RankingFacade 연결
    return ApiResponse.success(null);
  }

  @GetMapping("/breakout-ready")
  @Override
  public ApiResponse<BreakoutReadyResponse> getBreakoutReadyRanking() {
    // TODO: RankingFacade 연결
    return ApiResponse.success(null);
  }

  @GetMapping("/breakout-failed")
  @Override
  public ApiResponse<BreakoutFailedResponse> getBreakoutFailedRanking() {
    // TODO: RankingFacade 연결
    return ApiResponse.success(null);
  }
}
