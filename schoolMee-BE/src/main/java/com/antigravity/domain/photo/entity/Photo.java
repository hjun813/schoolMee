package com.antigravity.domain.photo.entity;

import com.antigravity.domain.school.entity.School;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사진 엔티티 (학교 단위 사진 풀).
 * AI가 분석한 smile_score, activity_score가 포함된다.
 * 원본 사진 1장이 여러 학생의 Chapter에 사용될 수 있음 (ChapterPhoto를 통해 N:M 해소).
 */
@Entity
@Table(name = "photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false, length = 1000)
    private String url;

    // AI 분석 결과: 미소 점수 (0 ~ 100)
    @Column(name = "smile_score")
    private Integer smileScore;

    // AI 분석 결과: 활동성 점수 (0 ~ 100)
    @Column(name = "activity_score")
    private Integer activityScore;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onUpload() {
        this.uploadedAt = LocalDateTime.now();
    }
}
