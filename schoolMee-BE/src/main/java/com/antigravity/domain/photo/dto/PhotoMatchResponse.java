package com.antigravity.domain.photo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhotoMatchResponse {
    private int processedPhotoCount;
    private int matchedStudentCount;
}
