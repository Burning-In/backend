package com.momentum.interfaces.api.sidebar;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.sidebar.SidebarV1Dto.AccountResponse;
import com.momentum.interfaces.api.sidebar.SidebarV1Dto.RecentlyViewedResponse;
import com.momentum.interfaces.api.sidebar.SidebarV1Dto.LikeStockResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Sidebar V1 API", description = "사이드바 관련 API 입니다.")
public interface SidebarV1ApiSpec {

    @Operation(
        summary = "계정 정보 조회",
        description = "사이드바에 표시할 로그인 여부 및 계정 정보를 조회합니다."
    )
    ApiResponse<AccountResponse> getAccount();

    @Operation(
        summary = "관심 종목 리스트 조회",
        description = "사이드바에 표시할 관심 종목 리스트를 조회합니다."
    )
    ApiResponse<LikeStockResponse> getLikeStocks();

    @Operation(
        summary = "최근 본 종목 리스트 조회",
        description = "사이드바에 표시할 최근 본 종목 리스트를 조회합니다."
    )
    ApiResponse<RecentlyViewedResponse> getRecentlyViewed();
}
