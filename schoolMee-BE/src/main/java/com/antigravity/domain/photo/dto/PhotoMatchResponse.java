package com.antigravity.domain.photo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 사진-학생 매칭 API 응답 DTO.
 * 생성된 PhotoStudent 매핑 수와 각 매칭 결과를 반환한다.
 */
@Getter
@Builder
public class PhotoMatchResponse {

    private int matchedCount;
    private List<PhotoMatchItem> matches;

    @Getter
    @Builder
    public static class PhotoMatchItem {
        private Long photoId;
        private Long studentId;
        private String studentName;
        private Double matchScore; // 0.0 ~ 1.0
    }
}
