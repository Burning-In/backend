package com.momentum.domain.stocktick;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.stock.TrackedStock;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockTick extends BaseEntity {

  private String tradeTime; // 애는 시간으로 받아야함, String이 아니라
  private Long price;
  private Long volume;
  private Long accVolume;
  private Double contractPower; // 애도 날리면 좋을것 같고
  private TrackedStock stockName;

  public StockTick(String tradeTime, Long price, Long volume, Long accVolume, Double contractPower, TrackedStock stockName) {
    this.tradeTime = tradeTime;
    this.price = price;
    this.volume = volume;
    this.accVolume = accVolume;
    this.contractPower = contractPower;
    this.stockName = stockName;
  }
}
