package com.antigravity.domain.story.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "themes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // 예: MINIMAL, CLASSIC, MODERN

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "cover_placeholder_url")
    private String coverPlaceholderUrl;
}
