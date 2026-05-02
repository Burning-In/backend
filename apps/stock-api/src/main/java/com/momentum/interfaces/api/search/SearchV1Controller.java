package com.momentum.interfaces.api.search;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.search.SearchV1Dto.StockSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/search")
public class SearchV1Controller implements SearchV1ApiSpec {

    @GetMapping("/stocks")
    @Override
    public ApiResponse<StockSearchResponse> searchStocks(
        @RequestParam(value = "query") String query
    ) {
        // TODO: SearchFacade 연결
        return ApiResponse.success(null);
    }
}
