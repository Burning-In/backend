package com.momentum.application;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.like.StockLikeRepository;
import com.momentum.domain.member.Member;
import com.momentum.domain.member.MemberRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.interfaces.api.stock.StockLikeV1Dto.LikeStockResponse;
import com.momentum.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockLikeServiceTest {

  @Autowired
  private StockLikeService stockLikeService;
  @Autowired
  private StockLikeRepository stockLikeRepository;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private StockRepository stockRepository;

  @Test
  @DisplayName("관심 종목을 추가하면 회원-종목 관심이 저장된다")
  void addLikeSavesStockLike() {
    Member member = saveMember("a@momentum.com");
    Stock stock = saveStock("000001");

    stockLikeService.addLike(member.getId(), stock.getCode());

    assertThat(stockLikeRepository.findByMemberAndStock(member, stock)).isPresent();
  }

  @Test
  @DisplayName("이미 추가한 종목을 다시 추가해도 중복 저장 없이 멱등하게 동작한다")
  void addLikeIsIdempotent() {
    Member member = saveMember("b@momentum.com");
    Stock stock = saveStock("000002");

    stockLikeService.addLike(member.getId(), stock.getCode());
    stockLikeService.addLike(member.getId(), stock.getCode());

    assertThat(stockLikeRepository.findAllByMember(member)).hasSize(1);
  }

  @Test
  @DisplayName("존재하지 않는 회원으로 추가하면 예외")
  void addLikeThrowsWhenMemberNotFound() {
    Stock stock = saveStock("000003");

    assertThatThrownBy(() -> stockLikeService.addLike(999_999L, stock.getCode()))
        .isInstanceOf(CoreException.class);
  }

  @Test
  @DisplayName("존재하지 않는 종목으로 추가하면 예외")
  void addLikeThrowsWhenStockNotFound() {
    Member member = saveMember("c@momentum.com");

    assertThatThrownBy(() -> stockLikeService.addLike(member.getId(), "999999"))
        .isInstanceOf(CoreException.class);
  }

  @Test
  @DisplayName("관심 종목을 해제하면 저장된 관심이 제거된다")
  void removeLikeDeletesStockLike() {
    Member member = saveMember("d@momentum.com");
    Stock stock = saveStock("000004");
    stockLikeService.addLike(member.getId(), stock.getCode());

    stockLikeService.removeLike(member.getId(), stock.getCode());

    assertThat(stockLikeRepository.findByMemberAndStock(member, stock)).isEmpty();
  }

  @Test
  @DisplayName("관심 종목이 아닌 종목을 해제해도 예외 없이 멱등하게 동작한다")
  void removeLikeIsIdempotent() {
    Member member = saveMember("e@momentum.com");
    Stock stock = saveStock("000005");

    stockLikeService.removeLike(member.getId(), stock.getCode());

    assertThat(stockLikeRepository.findAllByMember(member)).isEmpty();
  }

  @Test
  @DisplayName("관심 종목 목록은 종목 코드와 이름을 반환한다")
  void getLikeStocksReturnsCodeAndName() {
    Member member = saveMember("f@momentum.com");
    Stock first = saveStock("000006");
    Stock second = saveStock("000007");
    stockLikeService.addLike(member.getId(), first.getCode());
    stockLikeService.addLike(member.getId(), second.getCode());

    LikeStockResponse response = stockLikeService.getLikeStocks(member.getId());

    assertThat(response.stocks())
        .extracting(LikeStockResponse.LikeStockItem::stockCode)
        .containsExactlyInAnyOrder("000006", "000007");
    assertThat(response.stocks())
        .extracting(LikeStockResponse.LikeStockItem::stockName)
        .containsExactlyInAnyOrder("종목000006", "종목000007");
  }

  @Test
  @DisplayName("다른 회원의 관심 종목은 조회되지 않는다")
  void getLikeStocksReturnsOnlyOwnLikes() {
    Member owner = saveMember("g@momentum.com");
    Member other = saveMember("h@momentum.com");
    Stock stock = saveStock("000008");
    stockLikeService.addLike(owner.getId(), stock.getCode());

    LikeStockResponse response = stockLikeService.getLikeStocks(other.getId());

    assertThat(response.stocks()).isEmpty();
  }

  private Member saveMember(String email) {
    return memberRepository.save(Member.create(email, "password", "닉네임", "이름", "01000000000"));
  }

  private Stock saveStock(String code) {
    return stockRepository.save(new Stock("종목" + code, code, BREAKOUT_READY, StockTrend.UPTREND));
  }
}
