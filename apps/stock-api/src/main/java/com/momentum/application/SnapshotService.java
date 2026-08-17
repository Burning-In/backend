package com.momentum.application;

import com.momentum.domain.SnapshotJudgment;
import com.momentum.domain.SnapshotRepository;
import com.momentum.domain.StockSnapShot;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.infrastructure.query.SnapshotQueryDao;
import com.momentum.infrastructure.query.SnapshotRows.SnapshotListRow;
import com.momentum.infrastructure.query.SnapshotRows.SnapshotSourceRow;
import com.momentum.infrastructure.query.StockIdentityRow;
import com.momentum.infrastructure.query.StockMetaQueryDao;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotDetailResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotListResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotListResponse.SnapshotListItem;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotUpdateRequest;
import com.momentum.support.error.CoreException;
import com.momentum.support.error.ErrorType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SnapshotService {

  private final SnapshotRepository snapshotRepository;
  private final SnapshotQueryDao snapshotQueryDao;
  private final StockMetaQueryDao stockMetaQueryDao;

  @Transactional
  public SnapshotCreateResponse create(SnapshotCreateRequest request) {
    SnapshotSourceRow source = snapshotQueryDao.findSnapshotSource(request.stockCode(), LocalDate.now())
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND,
            "종목 또는 최신 캔들을 찾을 수 없습니다: " + request.stockCode()));

    List<StockSnapShot> references = snapshotRepository.findAllByIds(request.referenceSnapshotIds());
    LocalDateTime recordedAt = LocalDateTime.now();

    StockSnapShot snapshot = snapshotRepository.save(
        StockSnapShot.create(source.stockId(), StockRegime.valueOf(source.stockRegime()),
            source.closePrice(), request.judgment(), references, recordedAt, request.retrospective()));

    return new SnapshotCreateResponse(snapshot.getId(), recordedAt.toLocalDate());
  }

  @Transactional(readOnly = true)
  public SnapshotDetailResponse getDetail(Long snapshotId) {
    StockSnapShot snapshot = findSnapshot(snapshotId);
    StockIdentityRow stock = findStock(snapshot.getStockId());

    List<Long> referenceSnapshotIds = snapshot.getReferences().stream()
        .map(reference -> reference.getReferenced().getId())
        .toList();

    return new SnapshotDetailResponse(
        stock.stockName(),
        stock.stockCode(),
        snapshot.getJudgment(),
        referenceSnapshotIds,
        snapshot.getRecordedAt(),
        snapshot.getRetrospective());
  }

  // 추후에 커서 페이지네이션으로 수정필요
  @Transactional(readOnly = true)
  public SnapshotListResponse getSnapShots(LocalDateTime startDate, LocalDateTime endDate,
      List<SnapshotJudgment> judgments, List<StockRegime> regimes, String stockName) {
    List<SnapshotListRow> snapshots = snapshotQueryDao.search(
        startDate, endDate, namesOf(judgments), namesOf(regimes), stockName);

    List<SnapshotListItem> items = snapshots.stream()
        .map(snapshot -> new SnapshotListItem(
            snapshot.snapshotId(),
            snapshot.stockName(),
            snapshot.stockCode(),
            StockRegime.valueOf(snapshot.capturedRegime()),
            SnapshotJudgment.valueOf(snapshot.judgment()),
            snapshot.recordedAt(),
            snapshot.capturedPrice()))
        .toList();

    return new SnapshotListResponse(
        countOf(snapshots, SnapshotJudgment.BUY),
        countOf(snapshots, SnapshotJudgment.SELL),
        countOf(snapshots, SnapshotJudgment.WATCH),
        items);
  }

  @Transactional
  public void update(Long snapshotId, SnapshotUpdateRequest request) {
    StockSnapShot snapshot = findSnapshot(snapshotId);
    List<StockSnapShot> references = snapshotRepository.findAllByIds(request.referenceSnapshotIds());
    snapshot.update(request.judgment(), references, request.retrospective());
    snapshotRepository.save(snapshot);
  }

  private StockSnapShot findSnapshot(Long snapshotId) {
    return snapshotRepository.findById(snapshotId)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "스냅샷을 찾을 수 없습니다: " + snapshotId));
  }

  private StockIdentityRow findStock(Long stockId) {
    Map<Long, StockIdentityRow> stocks = stockMetaQueryDao.findIdentitiesByIds(List.of(stockId));
    StockIdentityRow stock = stocks.get(stockId);
    if (stock == null) {
      throw new CoreException(ErrorType.NOT_FOUND, "종목을 찾을 수 없습니다: " + stockId);
    }
    return stock;
  }

  private List<String> namesOf(List<? extends Enum<?>> values) {
    if (values == null) {
      return List.of();
    }
    return values.stream().map(Enum::name).toList();
  }

  private int countOf(List<SnapshotListRow> snapshots, SnapshotJudgment judgment) {
    return (int) snapshots.stream()
        .filter(snapshot -> SnapshotJudgment.valueOf(snapshot.judgment()) == judgment)
        .count();
  }
}
