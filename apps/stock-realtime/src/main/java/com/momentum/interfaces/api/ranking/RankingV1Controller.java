package com.momentum.interfaces.api.ranking;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.ranking.RankingV1Dto.FallReadyResponse;
import com.momentum.interfaces.api.ranking.RankingV1Dto.FallStartResponse;
import com.momentum.interfaces.api.ranking.RankingV1Dto.RiseReadyResponse;
import com.momentum.interfaces.api.ranking.RankingV1Dto.RiseStartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ranking")
public class RankingV1Controller implements RankingV1ApiSpec {

    @GetMapping("/rise-start")
    @Override
    public ApiResponse<RiseStartResponse> getRiseStartRanking() {
        // TODO: RankingFacade 연결
        return ApiResponse.success(null);
    }

    @GetMapping("/rise-ready")
    @Override
    public ApiResponse<RiseReadyResponse> getRiseReadyRanking() {
        // TODO: RankingFacade 연결
        return ApiResponse.success(null);
    }

    @GetMapping("/fall-start")
    @Override
    public ApiResponse<FallStartResponse> getFallStartRanking() {
        // TODO: RankingFacade 연결
        return ApiResponse.success(null);
    }

    @GetMapping("/fall-ready")
    @Override
    public ApiResponse<FallReadyResponse> getFallReadyRanking() {
        // TODO: RankingFacade 연결
        return ApiResponse.success(null);
    }

}
