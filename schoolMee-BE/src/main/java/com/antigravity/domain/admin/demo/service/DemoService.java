package com.antigravity.domain.admin.demo.service;

import com.antigravity.domain.photo.entity.AnalysisStatus;
import com.antigravity.domain.photo.entity.Photo;
import com.antigravity.domain.photo.entity.PhotoStudent;
import com.antigravity.domain.photo.entity.PhotoType;
import com.antigravity.domain.photo.repository.PhotoRepository;
import com.antigravity.domain.photo.repository.PhotoStudentRepository;
import com.antigravity.domain.school.entity.ClassRoom;
import com.antigravity.domain.school.entity.OnboardingStep;
import com.antigravity.domain.school.entity.School;
import com.antigravity.domain.school.repository.ClassRoomRepository;
import com.antigravity.domain.school.repository.SchoolRepository;
import com.antigravity.domain.story.entity.Chapter;
import com.antigravity.domain.story.entity.ChapterPhoto;
import com.antigravity.domain.story.entity.Story;
import com.antigravity.domain.story.entity.Theme;
import com.antigravity.domain.story.repository.StoryRepository;
import com.antigravity.domain.story.repository.ThemeRepository;
import com.antigravity.domain.student.entity.Student;
import com.antigravity.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoService {

    private final SchoolRepository schoolRepository;
    private final ClassRoomRepository classRoomRepository;
    private final StudentRepository studentRepository;
    private final PhotoRepository photoRepository;
    private final PhotoStudentRepository photoStudentRepository;
    private final StoryRepository storyRepository;
    private final ThemeRepository themeRepository;

    private final Random random = new Random();

    @Transactional
    public void setupDemoData() {
        log.info("데모 데이터 생성 시작...");

        // 1. School 생성
        School school = School.builder()
                .name("데모 초등학교")
                .onboardingStep(OnboardingStep.SCHOOL_CREATED)
                .build();
        school = schoolRepository.save(school);

        // 2. ClassRoom 생성
        ClassRoom classRoom = ClassRoom.builder()
                .school(school)
                .grade(3)
                .classNum(2)
                .build();
        classRoom = classRoomRepository.save(classRoom);
        school.updateOnboardingStep(OnboardingStep.CLASS_CREATED);

        // 3. Student 4명 생성
        String[] names = {"김민수", "이지훈", "박서연", "최유진"};
        List<Student> students = new ArrayList<>();
        for (String name : names) {
            Student student = Student.builder()
                    .classRoom(classRoom)
                    .name(name)
                    .faceKey("face_" + UUID.randomUUID())
                    .profileImagePath("uploads/photos/samples/profiles/dummy.jpg")
                    .build();
            students.add(studentRepository.save(student));
        }
        school.updateOnboardingStep(OnboardingStep.STUDENT_UPLOADED);

        // 4. Photo 5장 생성
        List<Photo> photos = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Photo photo = Photo.builder()
                    .school(school)
                    .url("uploads/photos/samples/group/demo_" + i + ".jpg")
                    .type(PhotoType.GROUP)
                    .smileScore(random.nextInt(40) + 60) // 60~100
                    .activityScore(random.nextInt(40) + 60)
                    .detectedFacesCount(random.nextInt(3) + 2) // 2~4
                    .analysisStatus(AnalysisStatus.ANALYZED)
                    .build();
            photos.add(photoRepository.save(photo));
        }
        school.updateOnboardingStep(OnboardingStep.PHOTO_UPLOADED);

        // 5. PhotoStudent 매핑 생성
        for (Photo photo : photos) {
            // 각 사진에 1~3명 랜덤 매칭
            int matchCount = random.nextInt(3) + 1;
            List<Integer> indices = new ArrayList<>(List.of(0, 1, 2, 3));
            java.util.Collections.shuffle(indices);
            
            for (int j = 0; j < matchCount; j++) {
                PhotoStudent ps = PhotoStudent.builder()
                        .photo(photo)
                        .student(students.get(indices.get(j)))
                        .matchScore(0.6 + (0.95 - 0.6) * random.nextDouble())
                        .build();
                photoStudentRepository.save(ps);
            }
        }
        school.updateOnboardingStep(OnboardingStep.MATCHING_COMPLETED);

        // 6. Story 생성
        Theme theme = themeRepository.findByCode("MINIMAL")
                .orElseGet(() -> themeRepository.findAll().get(0));

        for (Student student : students) {
            List<PhotoStudent> matchedPhotos = photoStudentRepository.findByStudentIdOrderByMatchScoreDesc(student.getId());
            
            if (matchedPhotos.size() >= 2) {
                Story story = Story.builder()
                        .student(student)
                        .theme(theme)
                        .title(student.getName() + "의 데모 앨범")
                        .summary("데모 데이터로 생성된 소중한 추억입니다.")
                        .coverImageUrl(matchedPhotos.get(0).getPhoto().getUrl())
                        .build();

                Chapter chapter = Chapter.builder()
                        .story(story)
                        .title("데모 챕터")
                        .sequence(1)
                        .build();

                for (PhotoStudent ps : matchedPhotos) {
                    ChapterPhoto cp = ChapterPhoto.builder()
                            .photo(ps.getPhoto())
                            .totalScore(100)
                            .build();
                    cp.assignChapter(chapter);
                }
                
                story.getChapters().add(chapter);
                storyRepository.save(story);
            }
        }
        school.updateOnboardingStep(OnboardingStep.STORY_GENERATED);

        log.info("데모 데이터 생성 완료 (School ID: {})", school.getId());
    }

    @Transactional
    public void resetDemoData() {
        log.info("데모 데이터 초기화 시작...");
        // "데모 초등학교" 이름을 가진 학교들 삭제 (데모용이므로 이름 기반 삭제 허용)
        List<School> demoSchools = schoolRepository.findAll().stream()
                .filter(s -> s.getName().equals("데모 초등학교"))
                .toList();
        
        schoolRepository.deleteAll(demoSchools);
        log.info("데모 데이터 초기화 완료 (삭제된 학교 수: {})", demoSchools.size());
    }
}
