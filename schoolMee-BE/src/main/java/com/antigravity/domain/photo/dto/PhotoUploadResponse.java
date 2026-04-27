package com.antigravity.domain.photo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 사진 업로드 API 응답 DTO.
 * 업로드된 사진 수와 각 파일의 저장 경로/상태를 반환한다.
 */
@Getter
@Builder
public class PhotoUploadResponse {

    private int uploadedCount;
    private List<PhotoUploadItem> photos;

    @Getter
    @Builder
    public static class PhotoUploadItem {
        private Long photoId;
        private String fileName;
        private String filePath;
        private String status; // PENDING
    }
}
