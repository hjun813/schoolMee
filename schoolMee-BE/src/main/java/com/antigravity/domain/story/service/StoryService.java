package com.antigravity.domain.story.service;

import com.antigravity.domain.photo.entity.Photo;
import com.antigravity.domain.photo.repository.PhotoRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final StudentRepository studentRepository;
    private final PhotoRepository photoRepository;

    private static final Random RANDOM = new Random();
    private static final List<String> CHAPTER_TITLES =
            List.of("두근두근 입학", "우리는 친구", "소중한 추억", "빛나는 졸업");

    /**
     * 학교 전체 학생에 대해 AI 스토리를 일괄 생성한다. (AI 시뮬레이션)
     * 이미 스토리가 있는 학생은 건너뜀 (멱등성 보장).
     *
     * [흐름]
     * 1. 학교의 사진 풀 조회
     * 2. 각 학생에 대해 Story + 4개 Chapter 생성
     * 3. 챕터별로 totalScore 기반 가중치 선별로 3~5장 사진 배정
     */
    @Transactional
    public StoryGenerateResponse generateStoriesForSchool(final Long schoolId) {
        final List<Student> students = studentRepository.findAllBySchoolIdWithSchool(schoolId);
        final List<Photo> photoPool = photoRepository.findBySchoolId(schoolId);

        if (photoPool.isEmpty()) {
            throw new IllegalStateException("사진 풀이 비어 있습니다. schoolId=" + schoolId);
        }

        int generated = 0;
        int skipped = 0;

        for (final Student student : students) {
            if (storyRepository.existsByStudentId(student.getId())) {
                skipped++;
                continue;
            }

            final Story story = Story.builder()
                    .student(student)
                    .title(student.getName() + "의 빛나는 이야기")
                    .build();

            for (int i = 0; i < CHAPTER_TITLES.size(); i++) {
                final Chapter chapter = Chapter.builder()
                        .story(story)
                        .title(CHAPTER_TITLES.get(i))
                        .sequence(i + 1)
                        .build();

                final List<Photo> selected = selectTopPhotos(photoPool, 3 + RANDOM.nextInt(3));
                for (final Photo photo : selected) {
                    chapter.getChapterPhotos().add(
                            ChapterPhoto.builder()
                                    .chapter(chapter)
                                    .photo(photo)
                                    .totalScore(photo.getSmileScore() + photo.getActivityScore())
                                    .build()
                    );
                }
                story.getChapters().add(chapter);
            }

            storyRepository.save(story);
            generated++;
        }

        return StoryGenerateResponse.builder()
                .schoolId(schoolId)
                .generated(generated)
                .skipped(skipped)
                .message(generated + "명 스토리 생성 완료, " + skipped + "명은 이미 존재하여 건너뜀")
                .build();
    }

    /**
     * 학교 단위 스토리 목록 조회 (관리자 검수 화면용).
     */
    @Transactional(readOnly = true)
    public StoryListResponse getStoriesBySchool(final Long schoolId) {
        final List<Story> stories = storyRepository.findAllBySchoolIdWithChapters(schoolId);
        return StoryListResponse.of(schoolId, stories);
    }

    /**
     * 특정 학생의 스토리 상세 조회 (Chapter + Photo 포함).
     */
    @Transactional(readOnly = true)
    public List<StoryResponse> getStoriesByStudent(final Long studentId) {
        return storyRepository.findAllByStudentIdWithDetails(studentId)
                .stream()
                .map(StoryResponse::from)
                .toList();
    }

    /**
     * 사진 풀에서 totalScore(smile+activity) 기반 확률 가중치로 n장을 선별한다.
     * 점수가 높은 사진이 더 높은 확률로 선택됨.
     */
    private List<Photo> selectTopPhotos(final List<Photo> pool, final int count) {
        final List<Photo> selected = new ArrayList<>();
        final List<Photo> shuffled = new ArrayList<>(pool);

        for (int i = 0; i < Math.min(count, pool.size()); i++) {
            if (shuffled.isEmpty()) break;

            final int currentTotalWeight = shuffled.stream()
                    .mapToInt(p -> p.getSmileScore() + p.getActivityScore())
                    .sum();

            if (currentTotalWeight <= 0) {
                selected.add(shuffled.remove(0));
                continue;
            }

            int pick = RANDOM.nextInt(currentTotalWeight);
            int cumulative = 0;
            Photo chosen = shuffled.get(0);

            for (final Photo photo : shuffled) {
                cumulative += photo.getSmileScore() + photo.getActivityScore();
                if (cumulative > pick) {
                    chosen = photo;
                    break;
                }
            }
            selected.add(chosen);
            shuffled.remove(chosen);
        }

        return selected;
    }
}
