package com.momentum.application.insight;

import com.momentum.domain.rs.KOPSIRelativeStrength;
import com.momentum.domain.rs.KOSPIRelativeStrengthRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.RsResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RsInsightService {

  private final KOSPIRelativeStrengthRepository kospiRelativeStrengthRepository;

  public RsResponse query(Stock stock, LocalDate at) {
    KOPSIRelativeStrength rs = kospiRelativeStrengthRepository.findLatestByStock(stock)
        .orElseThrow(() -> new NoSuchElementException("RS 데이터가 없습니다: " + stock.getCode()));

    BigDecimal rsValue = BigDecimal.valueOf(rs.getRsScore());
    return new RsResponse(rsValue, rsValue);
  }
}
