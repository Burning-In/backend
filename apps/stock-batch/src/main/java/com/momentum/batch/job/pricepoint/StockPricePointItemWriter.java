package com.momentum.batch.job.pricepoint;

import com.momentum.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockPricePointItemWriter implements ItemWriter<Stock> {

  @Override
  public void write(Chunk<? extends Stock> chunk) throws Exception {
  }
}
