package com.antigravity.domain.photo.entity;

import com.antigravity.domain.school.entity.School;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사진 엔티티 (학교 단위 사진 풀).
 *
 * [생명주기]
 * 1. 업로드 시: url, school, analysisStatus=PENDING 저장
 * 2. 분석 시: applyAnalysisResult()로 AI 시뮬레이션 결과 반영 → ANALYZED
 * 3. 매칭 시: PhotoStudent를 통해 Student와 N:M 연결
 *
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

    // AI 분석 결과: 미소 점수 (0 ~ 100). 분석 전 null.
    @Column(name = "smile_score")
    private Integer smileScore;

    // AI 분석 결과: 활동성 점수 (0 ~ 100). 분석 전 null.
    @Column(name = "activity_score")
    private Integer activityScore;

    // AI 분석 결과: 감지된 얼굴 수. 분석 전 null.
    @Column(name = "detected_faces_count")
    private Integer detectedFacesCount;

    // AI 분석 결과: 가상 얼굴 ID 목록 (CSV). ex) "face_1,face_2,face_3". 분석 전 null.
    @Column(name = "face_ids", length = 500)
    private String faceIds;

    // 사진 분석 상태 (PENDING / ANALYZED)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false, length = 20)
    private AnalysisStatus analysisStatus = AnalysisStatus.PENDING;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onUpload() {
        this.uploadedAt = LocalDateTime.now();
    }

    /**
     * AI 분석 시뮬레이션 결과를 반영한다.
     * @Transactional 컨텍스트 내에서 호출 시 dirty checking으로 자동 persist.
     */
    public void applyAnalysisResult(int smileScore, int activityScore,
                                    int detectedFacesCount, String faceIds) {
        this.smileScore = smileScore;
        this.activityScore = activityScore;
        this.detectedFacesCount = detectedFacesCount;
        this.faceIds = faceIds;
        this.analysisStatus = AnalysisStatus.ANALYZED;
    }
}
