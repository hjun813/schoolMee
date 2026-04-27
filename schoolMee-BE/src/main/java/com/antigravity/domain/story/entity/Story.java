package com.antigravity.domain.story.entity;

import com.antigravity.domain.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 스토리 엔티티. 핵심 콘텐츠 단위.
 * "사진이 아니라 스토리를 저장한다"는 설계 철학의 중심 엔티티.
 * 학생 1명당 1개의 Story를 생성하며, Story는 여러 Chapter로 구성된다.
 */
@Entity
@Table(name = "stories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String title;

    // Story -> Chapter: 양방향 관계. Chapter 생명주기는 Story에 종속.
    @org.hibernate.annotations.BatchSize(size = 20)
    @Builder.Default
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chapter> chapters = new ArrayList<>();

    // AI 회고 텍스트 (평균 점수 기반)
    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
