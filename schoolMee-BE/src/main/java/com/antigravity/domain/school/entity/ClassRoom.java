package com.antigravity.domain.school.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "class_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ClassRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false)
    private Integer grade;

    @Column(name = "class_num", nullable = false)
    private Integer classNum;
}
