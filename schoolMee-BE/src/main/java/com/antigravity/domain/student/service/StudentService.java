package com.antigravity.domain.student.service;

import com.antigravity.domain.student.dto.StudentProfileResponse;
import com.antigravity.global.storage.FileStorageService;
import com.antigravity.domain.order.repository.AlbumOrderRepository;
import com.antigravity.domain.school.repository.ClassRoomRepository;
import com.antigravity.domain.story.repository.StoryRepository;
import com.antigravity.domain.student.dto.StudentDetailResponse;
import com.antigravity.domain.student.dto.StudentListResponse;
import com.antigravity.domain.student.dto.StudentListResponse.StudentItem;
import com.antigravity.domain.student.entity.Student;
import com.antigravity.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final ClassRoomRepository classRoomRepository;
    private final StoryRepository storyRepository;
    private final AlbumOrderRepository albumOrderRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public StudentProfileResponse uploadProfilePhoto(Long studentId, MultipartFile file) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다. studentId=" + studentId));

        // 저장 디렉터리: uploads/photos/profiles/school_{id}
        String directory = "profiles/school_" + student.getSchool().getId();
        String path = fileStorageService.store(file, directory);

        // faceKey 설정. UUID 기반으로 고유성 보장
        String faceKey = "face_" + UUID.randomUUID().toString();
        student.updateProfile(path, faceKey);

        return StudentProfileResponse.builder()
                .studentId(student.getId())
                .faceKey(faceKey)
                .profileImagePath(path)
                .message("프로필 사진 등록 및 고유 식별 키 생성 완료")
                .build();
    }

    @Transactional
    public com.antigravity.domain.student.dto.StudentBulkUploadResponse bulkUploadProfilesAndCreateStudents(Long classRoomId, List<MultipartFile> files) {
        com.antigravity.domain.school.entity.ClassRoom classRoom = classRoomRepository.findById(classRoomId)
                .orElseThrow(() -> new IllegalArgumentException("반 정보를 찾을 수 없습니다. classRoomId=" + classRoomId));

        com.antigravity.domain.school.entity.School school = classRoom.getSchool();
        Long schoolId = school.getId();

        String directory = "profiles/school_" + schoolId;
        List<com.antigravity.domain.student.dto.StudentBulkUploadResponse.StudentProfileDto> createdStudents = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) continue;

            // 파일명 추출 로직 (e.g. honggildong.jpg -> honggildong)
            int dotIndex = originalFilename.lastIndexOf('.');
            String name = (dotIndex == -1) ? originalFilename : originalFilename.substring(0, dotIndex);
            
            // 이미지 저장
            String path = fileStorageService.store(file, directory);
            String faceKey = "face_" + UUID.randomUUID().toString();

            Student student = Student.builder()
                    .classRoom(classRoom)
                    .name(name)
                    .faceKey(faceKey)
                    .profileImagePath(path)
                    .build();

            studentRepository.save(student);

            createdStudents.add(com.antigravity.domain.student.dto.StudentBulkUploadResponse.StudentProfileDto.builder()
                    .studentId(student.getId())
                    .name(student.getName())
                    .faceKey(student.getFaceKey())
                    .build());
        }

        return com.antigravity.domain.student.dto.StudentBulkUploadResponse.builder()
                .totalUploaded(createdStudents.size())
                .createdStudents(createdStudents)
                .build();
    }

    /**
     * 학교 소속 학생 목록 조회.
     * hasStory, hasOrder 플래그를 포함하여 프론트에서 버튼 활성화 여부를 판단할 수 있다.
     */
    public StudentListResponse getStudentsBySchool(final Long schoolId) {
        final List<Student> students = studentRepository.findAllBySchoolIdWithSchool(schoolId);

        final List<StudentItem> items = students.stream()
                .map(s -> StudentItem.builder()
                        .studentId(s.getId())
                        .name(s.getName())
                        .grade(s.getClassRoom().getGrade())
                        .classNum(s.getClassRoom().getClassNum())
                        .hasStory(storyRepository.existsByStudentId(s.getId()))
                        .hasOrder(albumOrderRepository.existsByStudentId(s.getId()))
                        .build())
                .toList();

        return StudentListResponse.builder()
                .schoolId(schoolId)
                .students(items)
                .build();
    }

    /**
     * 학생 상세 조회.
     * 스토리 요약 정보(chapterCount, photoCount)를 포함하여 상세 카드 화면에서 사용.
     */
    public StudentDetailResponse getStudentDetail(final Long studentId) {
        final Student student = studentRepository.findByIdWithSchool(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다. studentId=" + studentId));

        // 스토리 조회 (없으면 null 허용)
        final var stories = storyRepository.findAllByStudentIdWithDetails(studentId);
        final var story = stories.isEmpty() ? null : stories.get(0);

        final int photoCount = story == null ? 0 :
                story.getChapters().stream().mapToInt(c -> c.getChapterPhotos().size()).sum();

        final boolean hasOrder = story != null && albumOrderRepository.existsByStoryId(story.getId());

        return StudentDetailResponse.builder()
                .studentId(student.getId())
                .name(student.getName())
                .grade(student.getClassRoom().getGrade())
                .classNum(student.getClassRoom().getClassNum())
                .schoolName(student.getSchool().getName())
                .storyId(story == null ? null : story.getId())
                .storyTitle(story == null ? null : story.getTitle())
                .chapterCount(story == null ? 0 : story.getChapters().size())
                .photoCount(photoCount)
                .hasOrder(hasOrder)
                .build();
    }

    /**
     * 레거시 faceKey 마이그레이션 (동명이인 방지용 UUID 전환)
     */
    @PostConstruct
    @Transactional
    public void migrateLegacyFaceKeys() {
        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            String faceKey = student.getFaceKey();
            // 레거시 패턴(face_{name})이거나 숫자 기반인 경우 UUID로 갱신
            if (faceKey != null && (!faceKey.contains("-") || faceKey.startsWith("face_student_"))) {
                String newKey = "face_" + UUID.randomUUID().toString();
                student.updateProfile(student.getProfileImagePath(), newKey);
            }
        }
    }
}
