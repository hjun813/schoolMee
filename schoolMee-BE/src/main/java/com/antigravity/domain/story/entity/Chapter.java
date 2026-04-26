package com.antigravity.domain.story.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 챕터 엔티티.
 * Story의 내러티브 단위. "입학", "친구", "추억", "졸업" 등의 테마로 구성.
 * sequence로 순서를 보장한다.
 */
@Entity
@Table(name = "chapters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(nullable = false)
    private String title;

    // 챕터 순서 (1: 입학, 2: 친구, 3: 추억, 4: 졸업)
    @Column(nullable = false)
    private Integer sequence;

    // Chapter -> ChapterPhoto: ChapterPhoto 생명주기는 Chapter에 종속.
    @org.hibernate.annotations.BatchSize(size = 50)
    @Builder.Default
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChapterPhoto> chapterPhotos = new ArrayList<>();
}
