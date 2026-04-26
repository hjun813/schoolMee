package com.antigravity.domain.story.service;

import com.antigravity.domain.photo.entity.Photo;
import com.antigravity.domain.photo.repository.PhotoRepository;
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

import java.util.*;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final StudentRepository studentRepository;
    private final PhotoRepository photoRepository;

    private static final Random RANDOM = new Random();

    // 챕터 테마 목록 (순서 고정)
    private static final List<String> CHAPTER_TITLES = List.of("두근두근 입학", "우리는 친구", "소중한 추억", "빛나는 졸업");

    /**
     * 특정 학교의 모든 학생에 대해 AI 스토리를 생성한다. (AI 시뮬레이션)
     *
     * [핵심 흐름]
     * 1. 학교의 사진 풀(Pool)을 가져온다.
     * 2. 각 학생에 대해 Story를 생성한다.
     * 3. 챕터별로 사진을 totalScore(smile+activity) 기준 가중치 랜덤 선별한다.
     * 4. ChapterPhoto에 AI 점수와 함께 저장한다.
     *
     * Trade-off: 현재는 동기 처리. 학교 규모가 커지면 @Async + Spring Batch로 전환 권장.
     */
    @Transactional
    public List<StoryResponse> generateStoriesForSchool(final Long schoolId) {
        // 학교에 속한 모든 학생 조회 (Fetch Join으로 최적화된 메서드 사용)
        final List<Student> students = studentRepository.findAllBySchoolIdWithSchool(schoolId);

        // 학교 사진 풀 조회
        final List<Photo> photoPool = photoRepository.findBySchoolId(schoolId);
        if (photoPool.isEmpty()) {
            throw new IllegalStateException("사진 풀이 비어 있습니다. schoolId=" + schoolId);
        }

        final List<Story> generatedStories = new ArrayList<>();

        for (final Student student : students) {
            // 이미 스토리가 있는 학생은 건너뜀 (멱등성 보장)
            if (storyRepository.existsByStudentId(student.getId())) {
                continue;
            }

            // Story 생성
            final Story story = Story.builder()
                    .student(student)
                    .title(student.getName() + "의 빛나는 이야기")
                    .build();

            // 챕터별 사진 선별 및 ChapterPhoto 생성
            for (int i = 0; i < CHAPTER_TITLES.size(); i++) {
                final Chapter chapter = Chapter.builder()
                        .story(story)
                        .title(CHAPTER_TITLES.get(i))
                        .sequence(i + 1)
                        .build();

                // 챕터당 랜덤으로 3~5장 선별 (totalScore 기준 가중치)
                final List<Photo> selected = selectTopPhotos(photoPool, 3 + RANDOM.nextInt(3));
                for (final Photo photo : selected) {
                    final int score = photo.getSmileScore() + photo.getActivityScore();
                    chapter.getChapterPhotos().add(
                            ChapterPhoto.builder()
                                    .chapter(chapter)
                                    .photo(photo)
                                    .totalScore(score)
                                    .build()
                    );
                }
                story.getChapters().add(chapter);
            }

            generatedStories.add(storyRepository.save(story));
        }

        return generatedStories.stream()
                .map(StoryResponse::from)
                .toList();
    }

    /**
     * 특정 학생의 스토리 목록을 조회한다.
     * Fetch Join으로 Chapter, ChapterPhoto, Photo를 한 번에 로드.
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
     * 점수가 높은 사진이 더 높은 확률로 선택된다.
     * (실제 AI라면 이 부분이 ML 모델 호출로 대체된다)
     */
    private List<Photo> selectTopPhotos(final List<Photo> pool, final int count) {
        final List<Photo> selected = new ArrayList<>();
        final List<Photo> shuffled = new ArrayList<>(pool);

        // 중복 없이 가중치 기반 선택
        for (int i = 0; i < Math.min(count, pool.size()); i++) {
            if (shuffled.isEmpty()) break;

            // 현재 남은 사진들의 전체 점수 합산 (매번 갱신)
            final int currentTotalWeight = shuffled.stream()
                    .mapToInt(p -> p.getSmileScore() + p.getActivityScore())
                    .sum();

            if (currentTotalWeight <= 0) {
                // 모든 사진의 점수가 0이면 그냥 첫 번째 사진 선택
                Photo chosen = shuffled.remove(0);
                selected.add(chosen);
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
