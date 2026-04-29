package com.antigravity.domain.story.service;

import com.antigravity.domain.photo.entity.Photo;
import com.antigravity.domain.photo.entity.PhotoStudent;
import com.antigravity.domain.photo.repository.PhotoStudentRepository;
import com.antigravity.domain.story.dto.StoryGenerateResponse;
import com.antigravity.domain.story.dto.StoryListResponse;
import com.antigravity.domain.story.dto.StoryResponse;
import com.antigravity.domain.story.entity.Chapter;
import com.antigravity.domain.story.entity.ChapterPhoto;
import com.antigravity.domain.story.entity.Story;
import com.antigravity.domain.story.repository.StoryRepository;
import com.antigravity.domain.student.entity.Student;
import com.antigravity.domain.student.repository.StudentRepository;
import com.antigravity.domain.school.repository.SchoolRepository;
import com.antigravity.domain.school.entity.OnboardingStep;
import com.antigravity.domain.school.entity.School;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final StudentRepository studentRepository;
    private final PhotoStudentRepository photoStudentRepository;
    private final SchoolRepository schoolRepository;
    private final com.antigravity.domain.story.repository.ThemeRepository themeRepository;

    private static final int MAX_PHOTOS_PER_STORY = 24;
    private static final int MIN_PHOTOS_PER_STORY = 3; // 사용자 요청에 따라 3장으로 조정
    private static final double MATCH_SCORE_THRESHOLD = 0.5;

    @Transactional
    public StoryGenerateResponse generateStoriesForSchool(final Long schoolId) {
        // 1. 학교 전체 학생 조회
        final List<Student> students = studentRepository.findAllBySchoolIdWithSchool(schoolId);
        final com.antigravity.domain.story.entity.Theme theme = themeRepository.findByCode("MINIMAL")
                .orElseGet(() -> themeRepository.findAll().get(0));

        int generated = 0;
        int skipped = 0;

        // 2. 학생 기준으로 루프 (사진 루프가 아님)
        for (final Student student : students) {
            // 중복 생성 방지: 이미 스토리가 있는 학생은 즉시 스킵
            if (student == null || storyRepository.existsByStudentId(student.getId())) {
                skipped++;
                continue;
            }

            // 3. 해당 학생의 모든 매칭 데이터 조회 및 임계값(0.5) 필터링
            final List<PhotoStudent> eligiblePhotos = 
                    photoStudentRepository.findByStudentIdOrderByMatchScoreDesc(student.getId())
                    .stream()
                    .filter(ps -> ps.getMatchScore() >= MATCH_SCORE_THRESHOLD)
                    .collect(Collectors.toList());

            // 4. 최소 사진 수(3장) 검증
            if (eligiblePhotos.size() < MIN_PHOTOS_PER_STORY) {
                log.info("학생 ID {} 스토리 생성 스킵: 유효 사진 부족 ({}장).", student.getId(), eligiblePhotos.size());
                skipped++;
                continue;
            }

            // 최대 사진 수 제한
            final List<PhotoStudent> selectedPhotos = eligiblePhotos.stream()
                    .limit(MAX_PHOTOS_PER_STORY)
                    .collect(Collectors.toList());

            // 5. 스토리 구성 요소 산출 (커버, 서머리 등)
            final String coverUrl = selectBestCover(selectedPhotos);
            final String summary = generateEnhancedSummary(selectedPhotos);

            final Story story = Story.builder()
                    .student(student)
                    .theme(theme)
                    .title(student.getName() + "의 빛나는 앨범")
                    .summary(summary)
                    .coverImageUrl(coverUrl)
                    .build();

            // 6. 챕터 전략 배분 및 저장
            assignPhotosToStrategicChapters(story, selectedPhotos);

            storyRepository.save(story);
            generated++;
            log.info("학생 ID {} 스토리 일괄 생성 완료 (사진 {}장)", student.getId(), selectedPhotos.size());
        }

        completeOnboarding(schoolId);

        return StoryGenerateResponse.builder()
                .schoolId(schoolId)
                .generated(generated)
                .skipped(skipped)
                .message(String.format("%d명 스토리 일괄 생성 완료 (기존/누락 %d명 제외)", generated, skipped))
                .build();
    }

    private String selectBestCover(List<PhotoStudent> photos) {
        return photos.stream()
                .map(PhotoStudent::getPhoto)
                .max((p1, p2) -> Integer.compare(
                    p1.getSmileScore() != null ? p1.getSmileScore() : 0,
                    p2.getSmileScore() != null ? p2.getSmileScore() : 0
                ))
                .map(Photo::getUrl)
                .orElse(null);
    }

    private String generateEnhancedSummary(List<PhotoStudent> photos) {
        final double avgSmile = photos.stream()
                .mapToInt(ps -> ps.getPhoto().getSmileScore() != null ? ps.getPhoto().getSmileScore() : 50)
                .average().orElse(50.0);
        final double avgActivity = photos.stream()
                .mapToInt(ps -> ps.getPhoto().getActivityScore() != null ? ps.getPhoto().getActivityScore() : 50)
                .average().orElse(50.0);
        
        return generateSummaryText(avgSmile, avgActivity);
    }

    private String generateSummaryText(double avgSmile, double avgActivity) {
        if (avgSmile >= 80 && avgActivity >= 80) return "열정 가득한 활동량과 끊이지 않는 웃음으로 가득 찬 한 해였습니다. 당신의 밝은 에너지가 주변을 환하게 만들었습니다.";
        if (avgSmile >= 70) return "다양한 활동 속에서도 친구들과 함께 환하게 웃는 모습이 인상적이었습니다. 소중한 추억들이 앨범에 가득 담겼습니다.";
        if (avgActivity >= 70) return "누구보다 적극적으로 참여하며 많은 것을 배운 한 해였습니다. 역동적인 순간들이 당신의 성장을 보여줍니다.";
        return "친구들과 함께한 소중한 기록들이 앨범에 오롯이 담겼습니다. 이 사진들이 훗날 꺼내보는 따뜻한 선물이 되기를 바랍니다.";
    }

    private void assignPhotosToStrategicChapters(Story story, List<PhotoStudent> selectedPhotos) {
        final Chapter opening = Chapter.builder().story(story).title("새로운 시작").sequence(1).build();
        final Chapter daily = Chapter.builder().story(story).title("소중한 일상").sequence(2).build();
        final Chapter friends = Chapter.builder().story(story).title("우리들의 시간").sequence(3).build();
        final Chapter event = Chapter.builder().story(story).title("특별한 순간").sequence(4).build();
        final Chapter closing = Chapter.builder().story(story).title("내일로 안녕").sequence(5).build();

        for (PhotoStudent ps : selectedPhotos) {
            final Photo p = ps.getPhoto();
            final ChapterPhoto cp = ChapterPhoto.builder()
                    .photo(p)
                    .totalScore((p.getSmileScore() != null ? p.getSmileScore() : 50) + 
                                (p.getActivityScore() != null ? p.getActivityScore() : 50))
                    .build();

            // 지능적 챕터 분배
            if (p.getType() == com.antigravity.domain.photo.entity.PhotoType.PROFILE) {
                cp.assignChapter(opening);
            } else if (p.getType() == com.antigravity.domain.photo.entity.PhotoType.EVENT) {
                cp.assignChapter(event);
            } else if (p.getType() == com.antigravity.domain.photo.entity.PhotoType.DAILY) {
                cp.assignChapter(daily);
            } else if (ps.getMatchScore() > 0.8) {
                cp.assignChapter(friends); // 매칭 점수 높음 -> 친구들과 함께/나 중심 활동
            } else {
                cp.assignChapter(closing);
            }
        }

        // 챕터 추가 (사진이 있는 것만)
        List.of(opening, daily, friends, event, closing).forEach(ch -> {
            if (!ch.getChapterPhotos().isEmpty()) story.getChapters().add(ch);
        });
    }

    @Transactional
    public void completeOnboarding(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("학교를 찾을 수 없습니다. schoolId=" + schoolId));
        
        school.updateOnboardingStep(OnboardingStep.STORY_GENERATED);
        log.info("학교 온보딩 단계 최종 완료: id={}, name={}, step={}", schoolId, school.getName(), OnboardingStep.STORY_GENERATED);
    }

    @Transactional(readOnly = true)
    public StoryListResponse getStoriesBySchool(final Long schoolId) {
        final List<Story> stories = storyRepository.findAllBySchoolIdWithChapters(schoolId);
        return StoryListResponse.of(schoolId, stories);
    }

    @Transactional(readOnly = true)
    public List<StoryResponse> getStoriesByStudent(final Long studentId) {
        return storyRepository.findAllByStudentIdWithDetails(studentId)
                .stream()
                .map(StoryResponse::from)
                .toList();
    }
}
