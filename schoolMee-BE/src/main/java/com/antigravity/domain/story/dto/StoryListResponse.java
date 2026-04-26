package com.antigravity.domain.story.dto;

import com.antigravity.domain.story.entity.Story;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 학교 단위 스토리 목록 응답 (관리자 검수 화면용).
 * Chapter 수, Photo 수를 포함하여 목록 화면에서 바로 현황 파악 가능.
 */
@Getter
@Builder
public class StoryListResponse {
    private Long schoolId;
    private List<StorySummary> stories;

    @Getter
    @Builder
    public static class StorySummary {
        private Long storyId;
        private Long studentId;
        private String studentName;
        private String title;
        private int chapterCount;
        private int totalPhotoCount;
        private LocalDateTime createdAt;

        public static StorySummary from(final Story story) {
            // 전체 사진 수 집계
            final int photoCount = story.getChapters().stream()
                    .mapToInt(c -> c.getChapterPhotos().size())
                    .sum();

            return StorySummary.builder()
                    .storyId(story.getId())
                    .studentId(story.getStudent().getId())
                    .studentName(story.getStudent().getName())
                    .title(story.getTitle())
                    .chapterCount(story.getChapters().size())
                    .totalPhotoCount(photoCount)
                    .createdAt(story.getCreatedAt())
                    .build();
        }
    }

    public static StoryListResponse of(final Long schoolId, final List<Story> stories) {
        return StoryListResponse.builder()
                .schoolId(schoolId)
                .stories(stories.stream().map(StorySummary::from).toList())
                .build();
    }
}
