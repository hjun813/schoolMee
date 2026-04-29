package com.antigravity.domain.student.entity;

import com.antigravity.domain.school.entity.ClassRoom;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_room_id", nullable = false)
    private ClassRoom classRoom;

    @Column(nullable = false)
    private String name;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    @Column(name = "face_key", unique = true)
    private String faceKey;

    public void updateProfile(String profileImagePath, String faceKey) {
        this.profileImagePath = profileImagePath;
        this.faceKey = faceKey;
    }

    public School getSchool() {
        return classRoom != null ? classRoom.getSchool() : null;
    }
}
