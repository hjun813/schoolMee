package com.antigravity.domain.photo.dto;

import com.antigravity.domain.photo.entity.Photo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 학교 사진 풀 현황 응답 DTO.
 * 스토리 생성 전 사진이 충분히 업로드되었는지 확인하는 용도.
 */
@Getter
@Builder
public class PhotoPoolResponse {
    private Long schoolId;
    private int totalPhotos;
    private List<PhotoInfo> photos;

    @Getter
    @Builder
    public static class PhotoInfo {
        private Long photoId;
        private String url;
        private Integer smileScore;
        private Integer activityScore;
        private LocalDateTime uploadedAt;
    }

    public static PhotoPoolResponse of(final Long schoolId, final List<Photo> photos) {
        return PhotoPoolResponse.builder()
                .schoolId(schoolId)
                .totalPhotos(photos.size())
                .photos(photos.stream()
                        .map(p -> PhotoInfo.builder()
                                .photoId(p.getId())
                                .url(p.getUrl())
                                .smileScore(p.getSmileScore())
                                .activityScore(p.getActivityScore())
                                .uploadedAt(p.getUploadedAt())
                                .build())
                        .toList())
                .build();
    }
}
