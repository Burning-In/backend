package com.momentum.domain.service;

import com.momentum.domain.entity.StockCandle;
import com.momentum.infrastructure.api.dto.StockChartInfoResponse;
import java.util.List;

public interface StockCandleService {

  List<StockCandle> create(String stockCode, StockChartInfoResponse stockChartInfoResponse);
}
