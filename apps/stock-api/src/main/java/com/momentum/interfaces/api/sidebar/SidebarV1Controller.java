package com.momentum.interfaces.api.sidebar;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.sidebar.SidebarV1Dto.AccountResponse;
import com.momentum.interfaces.api.sidebar.SidebarV1Dto.RecentlyViewedResponse;
import com.momentum.interfaces.api.sidebar.SidebarV1Dto.LikeStockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/sidebar")
public class SidebarV1Controller implements SidebarV1ApiSpec {

    @GetMapping("/account")
    @Override
    public ApiResponse<AccountResponse> getAccount() {
        // TODO: AccountFacade 연결
        return ApiResponse.success(null);
    }

    @GetMapping("/like-stocks")
    @Override
    public ApiResponse<LikeStockResponse> getLikeStocks() {
        // TODO: LikeStockFacade 연결
        return ApiResponse.success(null);
    }

    @GetMapping("/recently-viewed")
    @Override
    public ApiResponse<RecentlyViewedResponse> getRecentlyViewed() {
        // TODO: RecentlyViewedFacade 연결
        return ApiResponse.success(null);
    }
}
