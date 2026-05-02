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
        description = "상태/기간/종목 이름 필터를 적용하여 진행중 및 완료 스냅샷 목록을 조회합니다."
    )
    ApiResponse<SnapshotListResponse> getSnapshotList(
        @Schema(description = "상태 필터 (ONGOING / COMPLETED)") String status,
        @Schema(description = "시작(생성) 기간 - 시작") LocalDate startDateFrom,
        @Schema(description = "시작(생성) 기간 - 종료") LocalDate startDateTo,
        @Schema(description = "완료(종료) 기간 - 시작") LocalDate endDateFrom,
        @Schema(description = "완료(종료) 기간 - 종료") LocalDate endDateTo,
        @Schema(description = "종목 코드") String stockCode
    );

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
