package com.antigravity.domain.photo.entity;

import com.antigravity.domain.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Photo ↔ Student 중간 매핑 테이블.
 *
 * AI 분석으로 감지된 얼굴과 학생 간의 매칭 결과를 저장한다.
 * (photo_id, student_id) 복합 유니크 제약으로 중복 매칭을 방지한다.
 *
 * matchScore: smileScore * 0.6 + activityScore * 0.4 / 100.0 (0.0 ~ 1.0)
 * 실제 서비스에서는 얼굴 임베딩 벡터 코사인 유사도로 교체 예정.
 */
@Entity
@Table(
        name = "photo_students",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_photo_student",
                columnNames = {"photo_id", "student_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PhotoStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Photo photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student student;

    // 매칭 신뢰도 점수 (0.0 ~ 1.0)
    @Column(name = "match_score", nullable = false)
    private Double matchScore;
}
