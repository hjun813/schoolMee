package com.antigravity.domain.photo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhotoAnalysisResponse {
    private int processedCount;
}
