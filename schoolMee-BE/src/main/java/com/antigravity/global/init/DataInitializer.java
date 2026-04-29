package com.antigravity.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 앱 실행 시 더미 데이터를 자동으로 초기화하는 컴포넌트.
 *
 * [생성 데이터]
 * - School: 은하수초등학교 1개
 * - Student: 학생 10명 (6학년 3반)
 * - Photo: 사진 50장 (smile_score, activity_score 랜덤)
 *
 * application.yml의 ddl-auto: create-drop 설정과 함께 사용됨.
 * 앱 시작 시마다 테이블이 새로 생성되고, 이 Runner가 초기 데이터를 삽입한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final com.antigravity.domain.story.repository.ThemeRepository themeRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== 초기 테마 설정 중... ===");
        
        if (themeRepository.count() == 0) {
            themeRepository.save(com.antigravity.domain.story.entity.Theme.builder()
                    .code("MINIMAL")
                    .name("화이트 미니멀")
                    .description("깔끔함의 정석, 여백의 미를 강조한 스타일")
                    .build());
            themeRepository.save(com.antigravity.domain.story.entity.Theme.builder()
                    .code("CLASSIC")
                    .name("클래식 빈티지")
                    .description("따뜻한 색감과 질감으로 추억을 담는 스타일")
                    .build());
            themeRepository.save(com.antigravity.domain.story.entity.Theme.builder()
                    .code("MODERN")
                    .name("모던 컬러")
                    .description("세련된 폰트와 원색 포인트를 준 세련된 스타일")
                    .build());
            log.info("기본 테마 3종 등록 완료.");
        }

        log.info("=== SchoolMee Onboarding 모드: 일반 데이터 자동 생성을 건너뜁니다. ===");
    }
}
