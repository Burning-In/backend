package com.momentum.batch.job.regime;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockRegimeItemWriter implements ItemWriter<Stock> {

  private final StockRepository stockRepository;

  @Override
  public void write(Chunk<? extends Stock> chunk) throws Exception {
    List<? extends Stock> items = chunk.getItems();
    stockRepository.saveAll(new ArrayList<>(items));
  }
}
