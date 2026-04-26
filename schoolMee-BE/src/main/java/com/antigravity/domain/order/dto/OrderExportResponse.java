package com.antigravity.domain.order.dto;

import com.antigravity.domain.order.entity.AlbumOrder;
import com.antigravity.domain.order.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 인쇄 파트너에게 전달되는 JSON Export DTO.
 * Story → Chapter → Photo 전체 구조를 담는다.
 */
@Getter
@Builder
public class OrderExportResponse {

    private Long orderId;
    private OrderStatus status;
    private LocalDateTime orderedAt;
    private StudentInfo student;
    private StoryInfo story;

    @Getter
    @Builder
    public static class StudentInfo {
        private Long studentId;
        private String name;
        private String schoolName;
        private Integer grade;
        private Integer classNum;
    }

    @Getter
    @Builder
    public static class StoryInfo {
        private Long storyId;
        private String title;
        private List<ChapterInfo> chapters;
    }

    @Getter
    @Builder
    public static class ChapterInfo {
        private Long chapterId;
        private String title;
        private Integer sequence;
        private List<PhotoInfo> photos;
    }

    @Getter
    @Builder
    public static class PhotoInfo {
        private Long photoId;
        private String url;
        private AiScores aiScores;
    }

    @Getter
    @Builder
    public static class AiScores {
        private Integer smile;
        private Integer activity;
        private Integer total;
    }

    /** AlbumOrder Entity → Export DTO 변환 */
    public static OrderExportResponse from(final AlbumOrder order) {
        final var story = order.getStory();
        final var student = order.getStudent();

        final List<ChapterInfo> chapters = story.getChapters().stream()
                .sorted((a, b) -> a.getSequence() - b.getSequence())
                .map(chapter -> ChapterInfo.builder()
                        .chapterId(chapter.getId())
                        .title(chapter.getTitle())
                        .sequence(chapter.getSequence())
                        .photos(chapter.getChapterPhotos().stream()
                                .map(cp -> PhotoInfo.builder()
                                        .photoId(cp.getPhoto().getId())
                                        .url(cp.getPhoto().getUrl())
                                        .aiScores(AiScores.builder()
                                                .smile(cp.getPhoto().getSmileScore())
                                                .activity(cp.getPhoto().getActivityScore())
                                                .total(cp.getTotalScore())
                                                .build())
                                        .build())
                                .toList())
                        .build())
                .toList();

        return OrderExportResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .orderedAt(order.getCreatedAt())
                .student(StudentInfo.builder()
                        .studentId(student.getId())
                        .name(student.getName())
                        .schoolName(student.getSchool().getName())
                        .grade(student.getGrade())
                        .classNum(student.getClassNum())
                        .build())
                .story(StoryInfo.builder()
                        .storyId(story.getId())
                        .title(story.getTitle())
                        .chapters(chapters)
                        .build())
                .build();
    }
}
