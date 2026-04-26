package com.antigravity.domain.student.entity;

import com.antigravity.domain.school.entity.School;
import jakarta.persistence.*;
import lombok.*;

/**
 * 학생 엔티티.
 * 학교(School)에 속하며, 한 학생은 여러 Story를 가질 수 있다.
 * 학생은 별도 신청 없이 학교 계약 시 자동으로 앨범 대상이 된다.
 */
@Entity
@Table(name = "students")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 관계 - 여러 학생이 하나의 학교에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer grade;

    @Column(name = "class_num", nullable = false)
    private Integer classNum;
}
