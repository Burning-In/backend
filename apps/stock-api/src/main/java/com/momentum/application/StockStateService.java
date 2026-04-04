package com.momentum.application;

import com.momentum.application.dto.StockTickInfo;
import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCode;
import com.momentum.domain.entity.StockState;
import com.momentum.domain.entity.StockTick;
import com.momentum.domain.entity.StockTrend;
import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.entity.indicator.price.StockBaseType;
import com.momentum.domain.respository.StockBaseRepository;
import com.momentum.domain.respository.StockRepository;
import com.momentum.domain.respository.StockTickRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockStateService {

  private static final Double NOISE_THRESHOLD_PERCENT = 2.0;
  private static final Double STATE_CHANGE = 1.0;

  private final StockRepository stockRepository;
  private final StockBaseRepository stockBaseRepository;
  private final StockTickRepository stockTickRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  // 저항/지지로 주식 상태 정하는로직

  // 상승돌파 : 들어온 데이터가 기존 저항선을 돌파하면,
  // 상승가능 : 변동성 감소, 저항선 부근 왔다갔다(실시간)
  // 하락 가능 : 저항에 부딪치고, 돌파 실패후 하락
  // 하락 붕괴 : 지지선 이탈, 일단, 베이스의 최저지지 하락할떄 이탈
  //------------

  // # 실시간 데이터 -> 애는 실시간으로 받는거고, 들어온 데이터가 저앟ㅇ선을 넘었는지 아니면 어떻게 안넘었는지 보는거고
  // ## tick 수신
  // → evaluate(price)
  // → 상태 overwrite
  // ## 이전 상태와 다르면 이벤트 기록
  // → (노이즈라면 이벤트 생략 가능)  -> 노이즈의 기준은 2%정도?

  //---------
  // 뭘줘야하지?
  // 1. 실시간으로 감지하는 로직 -> How?, 이벤트 들어오는거 다 관리할거 아니긴하잖아, 일단 들어오는데이터랑 연뎔을 해야되고, 등등 뭐가 많은데 이거, 일단 DB 사용하낟고 치고
  // 2. 넘었으면 저장하는로직 -> 이벤트로 만들어서 히스토리로 남겨놓는과정 자체와 DTO를 그대로 받아서 저장하는것을 의미
  //------
  // 상태 변경 이벤트는 그래 이렇게 들어가는게 맞긴한데

  // 1. 웹소켓의 데이터를 기반으로, DB에서 해당 종목의 저항선//지지선 정보를 가져오고, 저항선이 넘으면 상승/지지선 내려가면 하락 -> 상태변경
  // 2. 상태 변경로직 : 이벤트나 나왔으면 이벤트를 따로 저장하던가(지금은 따로 저장하지 않고 이벤트 발행)

  /// -> 상태변경을 이벤트로 줄까 아니면, 지지선/저항 돌파를 이벤트로 줄까? -> 상태 변경을 이벤트로 가져가는게 맞다. 지지저항은 투머치
  // 3. 이벤트 보내는 것까지만?, DB저장을 여기서 처리해야되나?

  // 실시간 주가로 파악할 수 있는거는 하락붕괴랑 하락, 상승돌파
  // 상승 가능을 실시간으로 파악할 수가 있나? : 저항선에 근접하면 가능함 -> 상승의 케이스와 다르게 2%이내에 있으면
  @Transactional
  public void processTick(StockTickInfo stockTickInfo) {
    StockCode stockCode = StockCode.getCode(stockTickInfo.stockCode());
    Stock stock = stockRepository.findByStockCode(stockCode.getCode())
        .orElseThrow(IllegalStateException::new);
    StockBase stockBase = stockBaseRepository.findLastBase(stock.getId(), StockBaseType.CONFIRMED)
        .orElseThrow(IllegalArgumentException::new);

    // threshold가 넘는다고 생각을 하면, 상태를 변경하고, 이벤트를 발행합니다. -> 이거 어떻게 해야되는데
    // 상태를 던지면 ㅋㅋㅋ 어떻게 해야하나?
    // 아 긜고 상승 돞파, 상승 가능은 일단, 이평선 50, 150 200일이 정배열되어 있어야함 -> 그니깐 상승 추세여야함 이미
    StockStateEvent stockStateEvent = null; // 일단, 이벤트로 쓰고 어떻게 할지 생각

    // # 상승(저항선 돌파)
    // - 주식의 상태가 UpTrend(이평선 정배열)
    // - 이전 주식의 상태가 BreakOut이 아니여야함
    // - 저항선 돌파이후 2%이상 상승해야함, 상승한 가격이 어느정도 유지가 되었으면 좋겠는데, 어느정도 유지가 판단을 했으면 좋겠으
    // - 상승 돌파에 대한 정의 : 이전 저항선에서 2% 초과 상승, -->> 2%초과한 틱중에 (매수틱 > 매도틱), 아니 매수세가 더 강하면 안내려오겠지 ㄷㄷ, 매도세가 강하면 당연히 내려오고 -> 그니깐 이전략 말고 다른 전략으로 가야함
    // ---> “떨어지려고 해도 안 떨어지냐”, 저항선 위 2%가격이 5분이상 유지가 되는지 확인, 저항선을 넘었을떄부터 시간 측정(이걸 어캐하누.. 내가 가지고 있어야되는데ㄷ) : 서버 꺼지면 어캐하라고, 그떄 그냥 다시 계산해?..어.. 이것도 낫뱃인데?
    // ---> 다시 내려가도 바로 바꾸지 않고, 5분 이상 저항선 밑에 있으면 바꾼다.

    // 참 이게 5분이라는 정의가 쉽지는 않찌 -> 5분 이후에 박을 수도 있는거고, 최대한 틱데이터를 활용해보고 싶은데
    // 틱데이터를 활용한 falseBreakout 분석을 할 수가 있나?
    if (calculateGap(stockBase.getLowestSupportLinePrice(), stockTickInfo.currentPrice()) > NOISE_THRESHOLD_PERCENT
        && stock.getStockTrend().equals(StockTrend.UPTREND) && !stock.getStockState().equals(StockState.BREAKOUT)) {
      // 여기서 state가 생성된 시간이 5분이 지났으면,
      // 다시 가져와서 처리?, 아니면 틱데이터를 기준으로 5분을 잡을까? 5분동안인데, 이전에 하락했는지 안했는지 다 계산해야되고, 매번 계산해야되잖아 이거 ㅋㅋ

    }

    // # 상승 가능
    // - 주식의 상태가 UpTrend
    // - 베이스의 최대 지지선-저항선 안에있음
    // - 이전주식 상태가 상승가능이아님
    // - Vcp 생성되어야됨(변동성 축소가 있어야함)
    // ---->  저항선에 근접했으나 전날 상승하지 않은 종목들
    // ----> 이전날에 상승을해서 선에 딱 걸쳐있는 애들은 사면 안됨
    // - 저항선 근처까지 왔지만, 명확하게 맞고 떨어진적은 없음
    // - 이러면 선에 걸쳐있는 애들은 어떻게 해야되지?, 선에 걸쳐있는 애들도 같이 넣되 vcp애들이 우선순위로
    // - 눌림목도 추가하겠음


    // # 하락가능(저항선 아래로 하락중 : 일봉 전체로 봤을떄 하락추세였고, )
    // - 주식의 상태가 Other
    // - 이전 주식 상태가 하락가능이 아님
    // - 전날보다 오늘이 더 낮다 (하락 중)
    // - 일봉으로 봤을떄 저항선에서 맞고 떨어지는 구조고..전날과 비교했을떄도 계속 떨어지고 있음
    // “최근 저항선 근처까지 상승했지만 돌파에 실패했고, 이후 가격이 다시 하락하면서 ->  하락 흐름이 이어지거나 또는 변동성이 급격히 커지면서 가격이 불안정적으로”


    // # 하락(지지선 아래 붕괴)
    // - 주식의 상태가 Other
    // - 이전 주식 상태가 하락이 아님
    // - 일봉이 지지선 밑에 있을떄
    // - 지지선아래 2% 초과 하락, 지지선 2%아래로 5분이상 있을떄, 돌파와 같음

    applicationEventPublisher.publishEvent(stockStateEvent);

    // 추후 배치처리로 수정
    StockTick stockTick = new StockTick(stockTickInfo.tradeTime(), stockTickInfo.currentPrice(), stockTickInfo.tradeVolume(),
        stockTickInfo.accumulatedVolume(), stockCode);
    stockTickRepository.save(stockTick);
  }


  private double calculateGap(long linePrice, long currentPrice) {
    if (linePrice == 0.0) {
      return 0.0;
    }

    return ((double) (currentPrice - linePrice) / linePrice) * 100;
  }


  //-----------
  // # 일봉 데이터(보정 및 확정용)
  //  종가 수신
  // → evaluate(closePrice)
  // → 상태 overwrite (최종 확정)
  // → 이벤트 무조건 기록 (CONFIRMED)
  // → 구조 변경 (저항/지지/베이스), 이평선계산으로 Trend파악 -> 이것들이 있으나,, ㄴㄴ 바로 받아서 한다
  public void finalizeDailyState() {

  }
}
