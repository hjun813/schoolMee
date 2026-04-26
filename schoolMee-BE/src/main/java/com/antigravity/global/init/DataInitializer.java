package com.antigravity.global.init;

import com.antigravity.domain.photo.entity.Photo;
import com.antigravity.domain.photo.repository.PhotoRepository;
import com.antigravity.domain.school.entity.School;
import com.antigravity.domain.school.repository.SchoolRepository;
import com.antigravity.domain.student.entity.Student;
import com.antigravity.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

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

    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final PhotoRepository photoRepository;

    private static final Random RANDOM = new Random();

    // 더미 학생 이름
    private static final String[] STUDENT_NAMES = {
            "홍길동", "김철수", "이영희", "박민준", "최수아",
            "정지우", "강하늘", "윤서연", "임재원", "한소율"
    };

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("=== SchoolMee 더미 데이터 초기화 시작 ===");

        // 1. 학교 생성
        final School school = schoolRepository.save(
                School.builder()
                        .name("은하수초등학교")
                        .build()
        );
        log.info("학교 생성 완료: {} (id={})", school.getName(), school.getId());

        // 2. 학생 10명 생성 (6학년 3반)
        for (int i = 0; i < STUDENT_NAMES.length; i++) {
            final Student student = Student.builder()
                    .school(school)
                    .name(STUDENT_NAMES[i])
                    .grade(6)
                    .classNum(3)
                    .build();
            studentRepository.save(student);
        }
        log.info("학생 {}명 생성 완료", STUDENT_NAMES.length);

        // 3. 사진 50장 생성 (학교 사진 풀)
        //    실제 URL 대신 더미 S3 URL을 사용
        for (int i = 1; i <= 50; i++) {
            final int smileScore = 40 + RANDOM.nextInt(61);    // 40~100 랜덤
            final int activityScore = 30 + RANDOM.nextInt(71); // 30~100 랜덤

            final Photo photo = Photo.builder()
                    .school(school)
                    .url("https://schoolmee-dummy.s3.ap-northeast-2.amazonaws.com/schools/"
                            + school.getId() + "/photos/photo_" + i + ".jpg")
                    .smileScore(smileScore)
                    .activityScore(activityScore)
                    .build();
            photoRepository.save(photo);
        }
        log.info("사진 50장 생성 완료 (smile/activity 랜덤 점수 포함)");

        log.info("=== 더미 데이터 초기화 완료 ===");
        log.info("Swagger UI: http://localhost:8080/swagger-ui/index.html");
        log.info("학교 ID=1로 POST /api/v1/admin/stories/generate?schoolId=1 를 호출하여 스토리를 생성하세요.");
    }
}
