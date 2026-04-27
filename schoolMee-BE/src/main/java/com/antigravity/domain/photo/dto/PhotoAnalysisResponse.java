package com.antigravity.domain.photo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 사진 AI 분석 API 응답 DTO.
 * 분석된 사진 수와 각 사진의 AI 시뮬레이션 결과를 반환한다.
 */
@Getter
@Builder
public class PhotoAnalysisResponse {

    private int analyzedCount;
    private List<PhotoAnalysisItem> results;

    @Getter
    @Builder
    public static class PhotoAnalysisItem {
        private Long photoId;
        private Integer smileScore;       // 0~100
        private Integer activityScore;    // 0~100
        private Integer detectedFacesCount;
        private String faceIds;           // ex) "face_1,face_2,face_3"
        private String status;            // ANALYZED
    }
}
