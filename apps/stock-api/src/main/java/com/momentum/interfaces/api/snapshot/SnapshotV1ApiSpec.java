package com.momentum.interfaces.api.snapshot;

import com.momentum.domain.SnapshotJudgment;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotUpdateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotDetailResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Snapshot V1 API", description = "스냅샷 관련 API 입니다.")
public interface SnapshotV1ApiSpec {

    // ===================== Snapshot List Page =====================

    @Operation(
        summary = "스냅샷 목록 조회",
        description = """
            기간/판단/주식 레짐/종목명 필터를 적용하여 스냅샷 목록을 조회합니다.

            - 기간: startDate ~ endDate (년/월 단위, 미입력 시 전체)
            - 판단(judgments): BUY(매수), SELL(매도), WATCH(관망) — 복수 선택 가능, 미입력 시 전체
            - 주식 레짐(regimes): BREAKOUT_SUCCESS(돌파 성공), BREAKOUT_READY(돌파준비), BREAKOUT_FAILED(돌파실패), DOWNSIDE_BREAK(하방이탈), UNKNOWN(방향미정) — 복수 선택 가능, 미입력 시 전체
            - 종목명(stockName): 부분 일치 검색, 미입력 시 전체
            """
    )
    ApiResponse<SnapshotListResponse> getSnapshotList(
        @Schema(description = "조회 시작 날짜 (년/월/분)") LocalDateTime startDate,
        @Schema(description = "조회 종료 날짜 (년/월/분)") LocalDateTime endDate,
        @Schema(description = "판단 필터 (BUY, SELL, WATCH)") List<SnapshotJudgment> judgments,
        @Schema(description = "주식 레짐 필터 (BREAKOUT_SUCCESS, BREAKOUT_READY, BREAKOUT_FAILED, DOWNSIDE_BREAK, UNKNOWN)") List<StockRegime> regimes,
        @Schema(description = "종목명 검색어") String stockName
    );

    // ===================== Snapshot Detail =====================

    @Operation(
        summary = "스냅샷 상세 조회",
        description = "스냅샷의 참고한 스냅샷과 기록 시점(recordedAt)과 회고(retrospective)를 조회합니다. 지표 정보는 해당 시점의 insight API를 별도 호출하여 조회합니다."
    )
    ApiResponse<SnapshotDetailResponse> getSnapshotDetail(
        @Schema(description = "스냅샷 ID") Long snapshotId
    );

    // ===================== Snapshot Editor Overlay =====================

    @Operation(
        summary = "스냅샷 생성",
        description = "차트 설정 및 회고를 포함하여 스냅샷을 생성합니다."
    )
    ApiResponse<SnapshotCreateResponse> createSnapshot(
        SnapshotCreateRequest request
    );

    @Operation(
        summary = "스냅샷 수정",
        description = "판단(judgment), 참고 스냅샷 목록(referenceSnapshotIds), 회고(retrospective)를 수정합니다."
    )
    ApiResponse<Void> updateSnapshot(
        @Schema(description = "스냅샷 ID") Long snapshotId,
        SnapshotUpdateRequest request
    );
}
