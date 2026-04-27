package com.antigravity.domain.photo.repository;

import com.antigravity.domain.photo.entity.PhotoStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoStudentRepository extends JpaRepository<PhotoStudent, Long> {

    // 특정 사진에 매칭된 학생 목록 조회
    List<PhotoStudent> findByPhotoId(Long photoId);

    // 중복 매칭 방지용 존재 여부 확인
    boolean existsByPhotoIdAndStudentId(Long photoId, Long studentId);

    // 특정 학생의 매칭 사진 목록 (점수 높은 순)
    List<PhotoStudent> findByStudentIdOrderByMatchScoreDesc(Long studentId);
}
