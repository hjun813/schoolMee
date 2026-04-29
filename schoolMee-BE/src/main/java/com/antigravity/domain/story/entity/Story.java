package com.antigravity.domain.story.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private com.antigravity.domain.student.entity.Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @Column(nullable = false)
    private String title;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    // Story -> Chapter: 양방향 관계. Chapter 생명주기는 Story에 종속.
    @org.hibernate.annotations.BatchSize(size = 20)
    @Builder.Default
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private Set<Chapter> chapters = new HashSet<>();

    // AI 회고 텍스트 (평균 점수 기반)
    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.student == null) {
            throw new IllegalArgumentException("Story 생성 실패: 연관된 Student가 누락되었습니다.");
        }
        this.createdAt = LocalDateTime.now();
    }
}
