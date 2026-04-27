package com.antigravity.domain.story.dto;

import com.antigravity.domain.story.entity.ChapterPhoto;
import com.antigravity.domain.story.entity.Chapter;
import com.antigravity.domain.story.entity.Story;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Story 조회 응답 DTO.
 * Story → Chapter → Photo 구조를 중첩 DTO로 표현한다.
 */
@Getter
@Builder
public class StoryResponse {
    private Long storyId;
    private String title;
    private Long studentId;
    private String studentName;
    private LocalDateTime createdAt;
    private String summary;
    private List<ChapterDto> chapters;

    @Getter
    @Builder
    public static class ChapterDto {
        private Long chapterId;
        private String title;
        private Integer sequence;
        private List<PhotoDto> photos;
    }

    @Getter
    @Builder
    public static class PhotoDto {
        private Long photoId;
        private String url;
        private Integer totalScore;
    }

    /** Entity → DTO 변환 (정적 팩토리 메서드) */
    public static StoryResponse from(final Story story) {
        final List<ChapterDto> chapterDtos = story.getChapters().stream()
                .sorted((a, b) -> a.getSequence() - b.getSequence())
                .map(chapter -> ChapterDto.builder()
                        .chapterId(chapter.getId())
                        .title(chapter.getTitle())
                        .sequence(chapter.getSequence())
                        .photos(chapter.getChapterPhotos().stream()
                                .map(cp -> PhotoDto.builder()
                                        .photoId(cp.getPhoto().getId())
                                        .url(cp.getPhoto().getUrl())
                                        .totalScore(cp.getTotalScore())
                                        .build())
                                .toList())
                        .build())
                .toList();

        return StoryResponse.builder()
                .storyId(story.getId())
                .title(story.getTitle())
                .studentId(story.getStudent().getId())
                .studentName(story.getStudent().getName())
                .createdAt(story.getCreatedAt())
                .summary(story.getSummary())
                .chapters(chapterDtos)
                .build();
    }
}
