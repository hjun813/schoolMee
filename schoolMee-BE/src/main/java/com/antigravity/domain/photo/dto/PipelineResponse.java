package com.antigravity.domain.photo.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 통합 파이프라인 API 응답 DTO.
 * 업로드 → 분석 → 매칭 각 단계의 처리 수를 요약하여 반환한다.
 *
 * Story 생성은 POST /api/v1/admin/stories/generate?schoolId={schoolId} 로 별도 호출.
 */
@Getter
@Builder
public class PipelineResponse {

    private Long schoolId;
    private int uploadedCount;
    private int analyzedCount;
    private int matchedCount;
    private String message;
}
