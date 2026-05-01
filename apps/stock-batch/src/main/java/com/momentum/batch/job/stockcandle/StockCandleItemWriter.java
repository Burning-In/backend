package com.momentum.batch.job.stockcandle;

import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockCandleItemWriter implements ItemWriter<StockDailyCandle> {

  private final StockCandleRepository stockCandleRepository;

  @Override
  public void write(Chunk<? extends StockDailyCandle> chunk) throws Exception {
    List<? extends StockDailyCandle> items = chunk.getItems();
    stockCandleRepository.saveAll(new ArrayList<>(items));
  }
}
