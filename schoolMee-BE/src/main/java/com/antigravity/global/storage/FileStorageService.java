package com.antigravity.global.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 저장소 추상화 인터페이스.
 *
 * MVP: LocalFileStorageService (로컬 디스크)
 * 향후 교체: S3FileStorageService (AWS S3)
 *
 * 구현체를 교체해도 Service 계층 코드 변경 불필요.
 */
public interface FileStorageService {

    /**
     * 파일을 지정 디렉터리에 저장하고, 저장된 경로(또는 URL)를 반환한다.
     *
     * @param file      저장할 멀티파트 파일
     * @param directory 저장 대상 서브 디렉터리 (ex. "school_1")
     * @return 저장된 파일의 경로 또는 접근 URL
     */
    String store(MultipartFile file, String directory);

    /**
     * 지정 경로의 파일을 삭제한다.
     *
     * @param filePath 삭제할 파일 경로 또는 키
     */
    void delete(String filePath);
}
