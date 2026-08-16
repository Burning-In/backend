package com.momentum.domain.stocktick;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.stock.TrackedStock;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockTick extends BaseEntity {

  private static final DateTimeFormatter TRADE_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmmss");

  private LocalDateTime tradeTime; // 체결 시각 (분까지)
  private Long price;
  private Long volume;
  private Long accVolume;
  private TrackedStock stockName;

  private StockTick(LocalDateTime tradeTime, Long price, Long volume, Long accVolume, TrackedStock stockName) {
    this.tradeTime = tradeTime;
    this.price = price;
    this.volume = volume;
    this.accVolume = accVolume;
    this.stockName = stockName;
  }

  public static StockTick create(LocalDate tradeDate, String rawTradeTime, Long price, Long volume,
      Long accVolume, TrackedStock stockName) {
    LocalTime time = LocalTime.parse(rawTradeTime, TRADE_TIME_FORMAT);
    return new StockTick(tradeDate.atTime(time), price, volume, accVolume, stockName);
  }
}
