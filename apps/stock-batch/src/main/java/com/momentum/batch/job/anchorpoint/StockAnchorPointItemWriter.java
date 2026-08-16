package com.momentum.batch.job.anchorpoint;

import com.momentum.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockAnchorPointItemWriter implements ItemWriter<Stock> {

  @Override
  public void write(Chunk<? extends Stock> chunk) throws Exception {
  }
}
