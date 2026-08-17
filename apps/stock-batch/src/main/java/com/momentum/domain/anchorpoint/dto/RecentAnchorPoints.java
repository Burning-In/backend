package com.momentum.domain.anchorpoint.dto;

import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;

public record RecentAnchorPoints(
    StockAnchorPoint oldest,
    StockAnchorPoint previous,
    StockAnchorPoint target,
    StockAnchorPoint latest
) {

}
