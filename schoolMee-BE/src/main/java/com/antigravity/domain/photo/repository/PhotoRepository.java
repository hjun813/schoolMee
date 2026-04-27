package com.antigravity.domain.photo.repository;

import com.antigravity.domain.photo.entity.AnalysisStatus;
import com.antigravity.domain.photo.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

    // 학교 단위 사진 풀 조회 (AI 스토리 생성 시 사용)
    List<Photo> findBySchoolId(Long schoolId);

    // 분석 상태별 사진 조회 (분석 서비스: PENDING 조회 / 매칭 서비스: ANALYZED 조회)
    List<Photo> findBySchoolIdAndAnalysisStatus(Long schoolId, AnalysisStatus analysisStatus);
}

