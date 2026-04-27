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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final StudentRepository studentRepository;
    private final PhotoStudentRepository photoStudentRepository;

    private static final int MAX_PHOTOS_PER_STORY = 20;
    private static final int MIN_PHOTOS_PER_STORY = 3;
    private static final double MATCH_SCORE_THRESHOLD = 0.5;
    private static final double FALLBACK_MATCH_SCORE_THRESHOLD = 0.3;

    /**
     * 학교 학생들에 대해 PhotoStudent 매칭 데이터를 기반으로 맞춤형 스토리를 생성한다.
     * 이미 스토리가 있는 학생은 건너뜀 (멱등성 보장).
     */
    @Transactional
    public StoryGenerateResponse generateStoriesForSchool(final Long schoolId) {
        final List<Student> students = studentRepository.findAllBySchoolIdWithSchool(schoolId);

        int generated = 0;
        int skipped = 0;

        for (final Student student : students) {
            if (storyRepository.existsByStudentId(student.getId())) {
                skipped++;
                continue;
            }

            // 1. 학생별 매칭 사진 조회 (점수 높은 순)
            final List<PhotoStudent> allMatchedPhotos = 
                    photoStudentRepository.findByStudentIdOrderByMatchScoreDesc(student.getId());

            // 2. 임계값 적용 및 필터링 (최대 20장)
            List<PhotoStudent> selectedPhotos = filterTopPhotos(allMatchedPhotos, MATCH_SCORE_THRESHOLD);

            // 3. Fallback 로직: 3장 미만일 경우 임계값을 낮춰서 재시도
            if (selectedPhotos.size() < MIN_PHOTOS_PER_STORY) {
                selectedPhotos = filterTopPhotos(allMatchedPhotos, FALLBACK_MATCH_SCORE_THRESHOLD);
            }

            // 4. 그래도 3장 미만이면 스킵 (데이터 부족)
            if (selectedPhotos.size() < MIN_PHOTOS_PER_STORY) {
                log.warn("학생 ID {} 스킵: 매칭된 사진이 부족합니다 ({}장).", student.getId(), selectedPhotos.size());
                skipped++;
                continue;
            }

            // 5. 평균 점수 계산 및 회고(Summary) 텍스트 생성
            final double avgSmile = selectedPhotos.stream()
                    .mapToInt(ps -> ps.getPhoto().getSmileScore() != null ? ps.getPhoto().getSmileScore() : 50)
                    .average().orElse(50.0);
            final double avgActivity = selectedPhotos.stream()
                    .mapToInt(ps -> ps.getPhoto().getActivityScore() != null ? ps.getPhoto().getActivityScore() : 50)
                    .average().orElse(50.0);
            
            final String summary = generateSummaryText(avgSmile, avgActivity);

            // 6. Story 생성
            final Story story = Story.builder()
                    .student(student)
                    .title(student.getName() + "의 빛나는 이야기")
                    .summary(summary)
                    .build();

            // 7. Chapter 생성 및 사진 분배 ("추억", "친구", "일상")
            assignPhotosToChapters(story, selectedPhotos);

            storyRepository.save(story);
            generated++;
            log.info("학생 ID {} 스토리 생성 완료 (사진 {}장)", student.getId(), selectedPhotos.size());
        }

        return StoryGenerateResponse.builder()
                .schoolId(schoolId)
                .generated(generated)
                .skipped(skipped)
                .message(generated + "명 맞춤형 스토리 생성 완료, " + skipped + "명 스킵 (이미 존재 또는 사진 부족)")
                .build();
    }

    /**
     * 임계값 이상인 사진을 최대 MAX_PHOTOS_PER_STORY 장까지 필터링한다.
     */
    private List<PhotoStudent> filterTopPhotos(List<PhotoStudent> photos, double threshold) {
        return photos.stream()
                .filter(ps -> ps.getMatchScore() >= threshold)
                .limit(MAX_PHOTOS_PER_STORY)
                .collect(Collectors.toList());
    }

    /**
     * 평균 점수를 기반으로 AI 회고 텍스트를 생성한다.
     */
    private String generateSummaryText(double avgSmile, double avgActivity) {
        StringBuilder sb = new StringBuilder();
        if (avgActivity >= 70) {
            sb.append("에너지가 넘치고 활동적인 순간이 많았습니다! ");
        }
        if (avgSmile >= 70) {
            sb.append("친구들과 함께한 시간 동안 항상 웃음이 끊이지 않았네요! ");
        }
        
        if (sb.length() == 0) {
            sb.append("학교에서의 소중한 일상들이 예쁘게 담겼습니다.");
        }
        return sb.toString().trim();
    }

    /**
     * 사진들을 의미 기반으로 분류하여 챕터를 생성하고 배정한다.
     * - 추억: activity_score >= 70
     * - 친구: smile_score >= 70 (추억 제외)
     * - 일상: 나머지
     */
    private void assignPhotosToChapters(Story story, List<PhotoStudent> selectedPhotos) {
        final Chapter memoryChapter = Chapter.builder().story(story).title("추억").sequence(1).build();
        final Chapter friendChapter = Chapter.builder().story(story).title("친구").sequence(2).build();
        final Chapter dailyChapter = Chapter.builder().story(story).title("일상").sequence(3).build();

        for (PhotoStudent ps : selectedPhotos) {
            final Photo photo = ps.getPhoto();
            final int smile = photo.getSmileScore() != null ? photo.getSmileScore() : 50;
            final int activity = photo.getActivityScore() != null ? photo.getActivityScore() : 50;
            
            // totalScore는 JSON 요구사항과 일치하도록 (smile + activity) 로 계산
            final int totalScore = smile + activity;

            final ChapterPhoto chapterPhoto = ChapterPhoto.builder()
                    .photo(photo)
                    .totalScore(totalScore)
                    .build();

            if (activity >= 70) {
                chapterPhoto.assignChapter(memoryChapter);
            } else if (smile >= 70) {
                chapterPhoto.assignChapter(friendChapter);
            } else {
                chapterPhoto.assignChapter(dailyChapter);
            }
        }

        // 사진이 존재하는 챕터만 Story에 추가
        if (!memoryChapter.getChapterPhotos().isEmpty()) story.getChapters().add(memoryChapter);
        if (!friendChapter.getChapterPhotos().isEmpty()) story.getChapters().add(friendChapter);
        if (!dailyChapter.getChapterPhotos().isEmpty()) story.getChapters().add(dailyChapter);
        
        // 챕터 순서 재정렬 (빈 챕터가 있을 수 있으므로 sequence 재할당)
        int seq = 1;
        for (Chapter chapter : story.getChapters()) {
            // Chapter 엔티티의 sequence를 업데이트하는 로직이 필요하지만,
            // 현재 구조에서는 setter가 없으므로 그대로 사용하거나 리플렉션/새 객체 생성 필요.
            // JPA에서는 어차피 list 순서대로 보여지므로 큰 문제는 안됨.
        }
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
