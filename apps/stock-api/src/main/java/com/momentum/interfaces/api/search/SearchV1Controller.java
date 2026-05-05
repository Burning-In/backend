package com.momentum.interfaces.api.search;

import com.momentum.application.StockQueryService;
import com.momentum.domain.stock.Stock;
import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.search.SearchV1Dto.StockSearchResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/search")
public class SearchV1Controller implements SearchV1ApiSpec {

  private final StockQueryService stockQueryService;

  @GetMapping("/stocks")
  @Override
  public ApiResponse<StockSearchResponse> searchStocks(
      @RequestParam(value = "query") String query
  ) {
    List<Stock> stocks = stockQueryService.search(query);
    StockSearchResponse response = StockSearchResponse.from(stocks, null);
    return ApiResponse.success(response);
  }
}
