package com.momentum.batch.job.ma;

import com.momentum.domain.stock.Stock;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class StockMaItemWriter implements ItemWriter<Stock> {

  @Override
  public void write(Chunk<? extends Stock> chunk) throws Exception {
  }
}
