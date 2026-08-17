package com.momentum.application;

import static com.momentum.sharedkernel.StockRegime.BREAKOUT_READY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.like.StockLikeRepository;
import com.momentum.domain.member.Member;
import com.momentum.domain.member.MemberRepository;
import com.momentum.interfaces.api.stock.StockLikeV1Dto.LikeStockResponse;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.support.AnalysisTestData;
import com.momentum.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockLikeServiceTest {

  private static final String UNKNOWN_STOCK_CODE = "000270";

  @Autowired
  private StockLikeService stockLikeService;
  @Autowired
  private StockLikeRepository stockLikeRepository;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private AnalysisTestData analysisTestData;

  @Test
  @DisplayName("관심 종목을 추가하면 회원-종목 관심이 저장된다")
  void addLikeSavesStockLike() {
    Member member = saveMember("a@momentum.com");
    long stockId = saveStock("000040");

    stockLikeService.addLike(member.getId(), "000040");

    assertThat(stockLikeRepository.findByMemberAndStockId(member, stockId)).isPresent();
  }

  @Test
  @DisplayName("이미 추가한 종목을 다시 추가해도 중복 저장 없이 멱등하게 동작한다")
  void addLikeIsIdempotent() {
    Member member = saveMember("b@momentum.com");
    saveStock("000050");

    stockLikeService.addLike(member.getId(), "000050");
    stockLikeService.addLike(member.getId(), "000050");

    assertThat(stockLikeRepository.findAllByMember(member)).hasSize(1);
  }

  @Test
  @DisplayName("존재하지 않는 회원으로 추가하면 예외")
  void addLikeThrowsWhenMemberNotFound() {
    saveStock("000070");

    assertThatThrownBy(() -> stockLikeService.addLike(999_999L, "000070"))
        .isInstanceOf(CoreException.class);
  }

  @Test
  @DisplayName("존재하지 않는 종목으로 추가하면 예외")
  void addLikeThrowsWhenStockNotFound() {
    Member member = saveMember("c@momentum.com");

    assertThatThrownBy(() -> stockLikeService.addLike(member.getId(), UNKNOWN_STOCK_CODE))
        .isInstanceOf(CoreException.class);
  }

  @Test
  @DisplayName("관심 종목을 해제하면 저장된 관심이 제거된다")
  void removeLikeDeletesStockLike() {
    Member member = saveMember("d@momentum.com");
    long stockId = saveStock("000227");
    stockLikeService.addLike(member.getId(), "000227");

    stockLikeService.removeLike(member.getId(), "000227");

    assertThat(stockLikeRepository.findByMemberAndStockId(member, stockId)).isEmpty();
  }

  @Test
  @DisplayName("관심 종목이 아닌 종목을 해제해도 예외 없이 멱등하게 동작한다")
  void removeLikeIsIdempotent() {
    Member member = saveMember("e@momentum.com");
    saveStock("000540");

    stockLikeService.removeLike(member.getId(), "000540");

    assertThat(stockLikeRepository.findAllByMember(member)).isEmpty();
  }

  @Test
  @DisplayName("관심 종목 목록은 종목 코드와 이름을 반환한다")
  void getLikeStocksReturnsCodeAndName() {
    Member member = saveMember("f@momentum.com");
    saveStock("000480");
    saveStock("000490");
    stockLikeService.addLike(member.getId(), "000480");
    stockLikeService.addLike(member.getId(), "000490");

    LikeStockResponse response = stockLikeService.getLikeStocks(member.getId());

    assertSoftly(softly -> {
      softly.assertThat(response.stocks())
          .extracting(LikeStockResponse.LikeStockItem::stockCode)
          .containsExactlyInAnyOrder("000480", "000490");
      softly.assertThat(response.stocks())
          .extracting(LikeStockResponse.LikeStockItem::stockName)
          .containsExactlyInAnyOrder("종목000480", "종목000490");
    });
  }

  @Test
  @DisplayName("다른 회원의 관심 종목은 조회되지 않는다")
  void getLikeStocksReturnsOnlyOwnLikes() {
    Member owner = saveMember("g@momentum.com");
    Member other = saveMember("h@momentum.com");
    saveStock("000500");
    stockLikeService.addLike(owner.getId(), "000500");

    LikeStockResponse response = stockLikeService.getLikeStocks(other.getId());

    assertThat(response.stocks()).isEmpty();
  }

  private Member saveMember(String email) {
    return memberRepository.save(Member.create(email, "password", "닉네임", "이름", "01000000000"));
  }

  private long saveStock(String code) {
    return analysisTestData.saveStock(code, BREAKOUT_READY);
  }
}
