package com.momentum.interfaces.api.sidebar;

import java.util.List;

public class SidebarV1Dto {

    // ===================== Account Area =====================

    public record AccountResponse(
        boolean isLoggedIn,
        Long userId,
        String nickname
    ) {}

    // ===================== Like Stock (관심 종목 리스트) =====================

    public record LikeStockResponse(
        List<LikeStockItem> stocks
    ) {
        public record LikeStockItem(
            String stockCode,
            String stockName
        ) {}
    }

    // ===================== Recently Viewed (최근 본 종목 리스트) =====================

    public record RecentlyViewedResponse(
        List<RecentlyViewedItem> stocks
    ) {
        public record RecentlyViewedItem(
            String stockCode,
            String stockName,
            String recentlyViewedTerm
        ) {}
    }
}
