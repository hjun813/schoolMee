package com.antigravity.global.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/**
 * 로컬 디스크 기반 파일 저장 구현체.
 *
 * 저장 경로: {file.upload-dir}/{directory}/{uuid}.{ext}
 * ex) uploads/photos/school_1/a3f2-....jpg
 *
 * 향후 S3FileStorageService로 교체 시 이 클래스만 변경하고
 * FileStorageService 빈을 재등록하면 됨.
 */
@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload-dir:uploads/photos}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file, String directory) {
        try {
            final Path targetDir = Paths.get(uploadDir, directory).toAbsolutePath();
            Files.createDirectories(targetDir);

            final String originalFilename = file.getOriginalFilename();
            final String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            final String savedFilename = UUID.randomUUID() + extension;

            final Path targetPath = targetDir.resolve(savedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("파일 저장 완료: {}", targetPath);
            return targetPath.toString();

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            final boolean deleted = Files.deleteIfExists(Paths.get(filePath));
            if (deleted) {
                log.info("파일 삭제 완료: {}", filePath);
            } else {
                log.warn("삭제할 파일이 존재하지 않습니다: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", filePath, e);
        }
    }
}
