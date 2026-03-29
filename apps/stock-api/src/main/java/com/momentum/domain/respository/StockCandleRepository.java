package com.momentum.domain.respository;

import com.momentum.domain.entity.StockCandle;
import java.util.List;

public interface StockCandleRepository {

  List<StockCandle> save(List<StockCandle> dailyCandles);

}
