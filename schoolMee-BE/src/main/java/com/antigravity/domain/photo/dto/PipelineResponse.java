package com.antigravity.domain.photo.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 통합 파이프라인 API 응답 DTO.
 */
@Getter
@Builder
public class PipelineResponse {
    private int processedPhotos;
    private int storiesGenerated;
}
