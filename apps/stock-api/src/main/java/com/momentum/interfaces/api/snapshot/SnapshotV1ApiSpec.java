package com.momentum.interfaces.api.snapshot;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.CompletedDetailResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.OngoingDetailResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCompleteRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;

@Tag(name = "Snapshot V1 API", description = "스냅샷 관련 API 입니다.")
public interface SnapshotV1ApiSpec {

    // ===================== Snapshot List Page =====================

    @Operation(
        summary = "스냅샷 목록 조회",
        description = """
            기간/진행도/인사이트/정렬 필터를 적용하여 스냅샷 목록을 조회합니다.

            정렬 기준 (sort):
            - RECENT: 최근 순
            - PERIOD: 기간 순 (기간순은 회의 후 수정)
            - PROFIT_RATE: 수익률 순
            """
    )
    ApiResponse<SnapshotListResponse> getSnapshotList(
        @Schema(description = "조회 시작일") LocalDate startDate,
        @Schema(description = "조회 종료일") LocalDate endDate,
        @Schema(description = "진행 중 포함 여부") boolean includeOngoing,
        @Schema(description = "완료 포함 여부") boolean includeCompleted,
        @Schema(description = "인사이트 - 돌파시작") boolean breakoutStart,
        @Schema(description = "인사이트 - 돌파준비") boolean breakoutReady,
        @Schema(description = "인사이트 - 돌파실패") boolean breakoutFailed,
        @Schema(description = "인사이트 - 하방이탈") boolean downsideBreak,
        @Schema(description = "인사이트 - 방향미정") boolean undetermined,
        @Schema(description = "인사이트 - 1년 모멘텀") boolean momentum,
        @Schema(description = "인사이트 - 흐름 안정도(FIP)") boolean fip,
        @Schema(description = "인사이트 - 이동평균선") boolean movingAverage,
        @Schema(description = "인사이트 - 거래량") boolean volume,
        @Schema(description = "인사이트 - EPS") boolean eps,
        @Schema(description = "인사이트 - RS") boolean rs,
        @Schema(description = "정렬 기준 (RECENT / PERIOD / PROFIT_RATE)") String sort
    );

    // 스냅샷 crud는 회의 후 진행, 세부조회도 해야함
    // ===================== Snapshot Editor Overlay =====================

    @Operation(
        summary = "스냅샷 생성 (시작)",
        description = "차트 설정 및 메모를 포함하여 시작 스냅샷을 생성합니다."
    )
    ApiResponse<SnapshotCreateResponse> createSnapshot(
        SnapshotCreateRequest request
    );

    @Operation(
        summary = "스냅샷 완료 처리 (완)",
        description = "진행중인 스냅샷을 완료 처리합니다."
    )
    ApiResponse<Void> completeSnapshot(
        @Schema(description = "스냅샷 ID") Long snapshotId,
        SnapshotCompleteRequest request
    );

    // ===================== Snapshot Ongoing Detail Page =====================

    @Operation(
        summary = "진행중 스냅샷 상세 조회",
        description = "진행중 스냅샷의 시작 시점 기록과 현재 시점 기록을 조회합니다."
    )
    ApiResponse<OngoingDetailResponse> getOngoingDetail(
        @Schema(description = "스냅샷 ID") Long snapshotId
    );

    // ===================== Snapshot Completed Detail Page =====================

    @Operation(
        summary = "완료 스냅샷 상세 조회",
        description = "완료 스냅샷의 요약 결과, 시작 기록, 종료 기록을 조회합니다."
    )
    ApiResponse<CompletedDetailResponse> getCompletedDetail(
        @Schema(description = "스냅샷 ID") Long snapshotId
    );

    // ===================== 공통 =====================

    @Operation(
        summary = "스냅샷 삭제",
        description = "진행중 또는 완료 스냅샷을 삭제합니다."
    )
    ApiResponse<Void> deleteSnapshot(
        @Schema(description = "스냅샷 ID") Long snapshotId
    );
}
