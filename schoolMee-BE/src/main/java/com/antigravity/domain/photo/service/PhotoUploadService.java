package com.antigravity.domain.photo.service;

import com.antigravity.domain.photo.dto.PhotoUploadResponse;
import com.antigravity.domain.photo.entity.AnalysisStatus;
import com.antigravity.domain.photo.entity.Photo;
import com.antigravity.domain.photo.repository.PhotoRepository;
import com.antigravity.domain.school.entity.School;
import com.antigravity.domain.school.repository.SchoolRepository;
import com.antigravity.global.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 사진 업로드 서비스.
 *
 * [흐름]
 * 1. 학교 ID 유효성 검증
 * 2. FileStorageService를 통해 파일 저장 (로컬 or S3 — 인터페이스 교체 가능)
 * 3. Photo 엔티티 저장 (analysisStatus=PENDING)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoUploadService {

    private final PhotoRepository photoRepository;
    private final SchoolRepository schoolRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public PhotoUploadResponse uploadPhotos(Long schoolId, MultipartFile[] files) {
        final School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "학교를 찾을 수 없습니다. schoolId=" + schoolId));

        final List<PhotoUploadResponse.PhotoUploadItem> items = new ArrayList<>();

        for (MultipartFile file : files) {
            // FileStorageService 구현체에 따라 로컬 or S3에 저장
            final String filePath = fileStorageService.store(file, "school_" + schoolId);

            final Photo photo = Photo.builder()
                    .school(school)
                    .url(filePath)
                    .analysisStatus(AnalysisStatus.PENDING)
                    .build();

            final Photo saved = photoRepository.save(photo);

            items.add(PhotoUploadResponse.PhotoUploadItem.builder()
                    .photoId(saved.getId())
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath)
                    .status(AnalysisStatus.PENDING.name())
                    .build());

            log.info("사진 업로드 완료: photoId={}, fileName={}", saved.getId(), file.getOriginalFilename());
        }

        log.info("총 {}장 업로드 완료 (schoolId={})", items.size(), schoolId);

        return PhotoUploadResponse.builder()
                .uploadedCount(items.size())
                .photos(items)
                .build();
    }
}
