package com.antigravity.domain.photo.repository;

import com.antigravity.domain.photo.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

    // 학교 단위 사진 풀 조회 (AI 스토리 생성 시 사용)
    List<Photo> findBySchoolId(Long schoolId);
}
