package com.momentum.interfaces.api.stock;

import com.momentum.application.StockLikeService;
import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockLikeV1Dto.LikeStockResponse;
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
@RequestMapping("/api/v1/stocks")
public class StockLikeV1Controller implements StockLikeV1ApiSpec {

  private final StockLikeService stockLikeService;

  @PostMapping("/{stockCode}/like")
  @Override
  public ApiResponse<Void> addLike(
      @PathVariable String stockCode,
      @RequestParam Long memberId
  ) {
    stockLikeService.addLike(memberId, stockCode);
    return ApiResponse.success(null);
  }

  @DeleteMapping("/{stockCode}/like")
  @Override
  public ApiResponse<Void> removeLike(
      @PathVariable String stockCode,
      @RequestParam Long memberId
  ) {
    stockLikeService.removeLike(memberId, stockCode);
    return ApiResponse.success(null);
  }

  @GetMapping("/likes")
  @Override
  public ApiResponse<LikeStockResponse> getLikeStocks(
      @RequestParam Long memberId
  ) {
    return ApiResponse.success(stockLikeService.getLikeStocks(memberId));
  }
}
