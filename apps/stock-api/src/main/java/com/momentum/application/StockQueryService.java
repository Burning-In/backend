package com.momentum.application;

import com.momentum.infrastructure.query.StockMetaQueryDao;
import com.momentum.infrastructure.query.StockMetaRow;
import com.momentum.support.error.CoreException;
import com.momentum.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockQueryService {

  private final StockMetaQueryDao stockMetaQueryDao;

  public List<StockMetaRow> search(String query) {
    return stockMetaQueryDao.search(query);
  }

  public StockMetaRow getByCode(String stockCode) {
    return stockMetaQueryDao.findByCode(stockCode)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "종목을 찾을 수 없습니다: " + stockCode));
  }
}
