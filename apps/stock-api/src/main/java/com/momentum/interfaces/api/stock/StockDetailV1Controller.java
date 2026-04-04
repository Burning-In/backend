package com.momentum.interfaces.api.stock;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockDetailV1Dto.CandleChartResponse;
import com.momentum.interfaces.api.stock.StockDetailV1Dto.EpsResponse;
import com.momentum.interfaces.api.stock.StockDetailV1Dto.ExpectedReturnResponse;
import com.momentum.interfaces.api.stock.StockDetailV1Dto.InsightResponse;
import com.momentum.interfaces.api.stock.StockDetailV1Dto.PbResponse;
import com.momentum.interfaces.api.stock.StockDetailV1Dto.SummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/stocks/{stockCode}")
public class StockDetailV1Controller implements StockDetailV1ApiSpec {

    @GetMapping("/summary")
    @Override
    public ApiResponse<SummaryResponse> getSummary(
        @PathVariable String stockCode
    ) {
        // TODO: StockDetailFacade 연결
        return ApiResponse.success(null);
    }

    @GetMapping("/chart")
    @Override
    public ApiResponse<CandleChartResponse> getCandleChart(
        @PathVariable String stockCode,
        @RequestParam(value = "timeline", required = false) String timeline
    ) {
        // TODO: StockDetailFacade 연결
        return ApiResponse.success(null);
    }

    @GetMapping("/insight")
    @Override
    public ApiResponse<InsightResponse> getInsight(
        @PathVariable String stockCode
    ) {
        // TODO: StockDetailFacade 연결
        return ApiResponse.success(null);
    }

    @GetMapping("/pb")
    @Override
    public ApiResponse<PbResponse> getPb(
        @PathVariable String stockCode
    ) {
        // TODO: StockDetailFacade 연결
        return ApiResponse.success(null);
    }

    @GetMapping("/eps")
    @Override
    public ApiResponse<EpsResponse> getEps(
        @PathVariable String stockCode
    ) {
        // TODO: StockDetailFacade 연결
        return ApiResponse.success(null);
    }

    @GetMapping("/expected-return")
    @Override
    public ApiResponse<ExpectedReturnResponse> getExpectedReturn(
        @PathVariable String stockCode
    ) {
        // TODO: StockDetailFacade 연결
        return ApiResponse.success(null);
    }

    @PostMapping("/like")
    @Override
    public ApiResponse<Void> addLike(
        @PathVariable String stockCode
    ) {
        // TODO: LikeStockFacade 연결
        return ApiResponse.success(null);
    }

    @DeleteMapping("/like")
    @Override
    public ApiResponse<Void> removeLike(
        @PathVariable String stockCode
    ) {
        // TODO: LikeStockFacade 연결
        return ApiResponse.success(null);
    }

}
