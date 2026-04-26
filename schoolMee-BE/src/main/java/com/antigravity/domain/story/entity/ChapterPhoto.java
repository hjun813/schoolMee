package com.antigravity.domain.story.entity;

import com.antigravity.domain.photo.entity.Photo;
import jakarta.persistence.*;
import lombok.*;

/**
 * 챕터-사진 매핑 엔티티 (N:M 해소 테이블).
 * Chapter와 Photo의 다대다 관계를 풀어낸 중간 테이블.
 * AI 선별 결과(totalScore)를 이 테이블에 저장한다.
 * 동일한 원본 사진이 여러 학생의 챕터에 포함될 수 있으므로 필수적인 설계.
 */
@Entity
@Table(name = "chapter_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChapterPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", nullable = false)
    private Photo photo;

    // AI 분석 결과 - 해당 챕터에서의 이 사진의 종합 점수 (smileScore + activityScore)
    @Column(name = "total_score")
    private Integer totalScore;
}
