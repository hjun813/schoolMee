package com.antigravity.domain.print.service;

import com.antigravity.domain.order.entity.AlbumOrder;
import com.antigravity.domain.order.repository.AlbumOrderRepository;
import com.antigravity.domain.print.dto.ElementDto;
import com.antigravity.domain.print.dto.PageDto;
import com.antigravity.domain.print.dto.PrintPackageResponse;
import com.antigravity.domain.story.entity.Chapter;
import com.antigravity.domain.story.entity.ChapterPhoto;
import com.antigravity.domain.story.entity.Story;
import com.antigravity.domain.story.repository.StoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrintPackageService {

    private final StoryRepository storyRepository;
    private final AlbumOrderRepository orderRepository;

    @Transactional(readOnly = true)
    public PrintPackageResponse generate(Long storyId) {
        final Story story = storyRepository.findByIdWithDetails(storyId)
                .orElseThrow(() -> new IllegalArgumentException("스토리 찾을 수 없음: " + storyId));

        // AlbumOrder를 찾아 orderId를 확보 (존재하지 않을 경우 대비해 Optional 처리)
        Long orderId = orderRepository.findByStoryId(storyId)
                .map(AlbumOrder::getId)
                .orElse(null);

        final List<PageDto> pages = new ArrayList<>();
        int pageNum = 1;

        // 1. Cover Page 생성
        pages.add(createCoverPage(pageNum++, story));

        // 2. Chapter 별로 본문 페이지(1장당 3개의 사진 chunk) 생성
        for (Chapter chapter : story.getChapters()) {
            List<ChapterPhoto> photos = new ArrayList<>(chapter.getChapterPhotos());
            
            // 최대 3장씩 끊어서 페이지 생성
            for (int i = 0; i < photos.size(); i += 3) {
                int end = Math.min(i + 3, photos.size());
                List<ChapterPhoto> chunk = photos.subList(i, end);
                pages.add(createChapterPage(pageNum++, chapter.getTitle(), chunk));
            }
        }

        return PrintPackageResponse.builder()
                .orderId(orderId)
                .storyId(storyId)
                .studentName(story.getStudent().getName())
                .summary(story.getSummary())
                .pages(pages)
                .build();
    }

    private PageDto createCoverPage(int pageNum, Story story) {
        return PageDto.builder()
                .pageNumber(pageNum)
                .type("cover")
                .elements(List.of(
                        ElementDto.builder().type("text").contentOrUrl(story.getTitle() != null ? story.getTitle() : "추억 이야기").x(50).y(100).build(),
                        ElementDto.builder().type("text").contentOrUrl(story.getStudent().getName()).x(50).y(140).build()
                ))
                .build();
    }

    private PageDto createChapterPage(int pageNum, String title, List<ChapterPhoto> chunk) {
        List<ElementDto> elements = new ArrayList<>();
        // 챕터 타이틀 배치
        elements.add(ElementDto.builder().type("text").contentOrUrl("Chapter: " + title).x(10).y(10).build());

        // MVP 기준 하드코딩된 레이아웃 (페이지 210x297 기준 가상)
        if (chunk.size() == 1) {
            elements.add(buildImageElement(chunk.get(0).getPhoto().getUrl(), 10, 30, 190, 190));
        } else if (chunk.size() == 2) {
            elements.add(buildImageElement(chunk.get(0).getPhoto().getUrl(), 10, 30, 190, 100));
            elements.add(buildImageElement(chunk.get(1).getPhoto().getUrl(), 10, 140, 190, 100));
        } else if (chunk.size() == 3) {
            elements.add(buildImageElement(chunk.get(0).getPhoto().getUrl(), 10, 30, 190, 100)); // 상단 1장
            elements.add(buildImageElement(chunk.get(1).getPhoto().getUrl(), 10, 140, 90, 100));  // 하단 2장 분할 (좌)
            elements.add(buildImageElement(chunk.get(2).getPhoto().getUrl(), 110, 140, 90, 100)); // 하단 2장 분할 (우)
        }

        return PageDto.builder()
                .pageNumber(pageNum)
                .type("chapter")
                .elements(elements)
                .build();
    }

    private ElementDto buildImageElement(String url, int x, int y, int w, int h) {
        return ElementDto.builder()
                .type("image")
                .contentOrUrl(url)
                .x(x)
                .y(y)
                .width(w)
                .height(h)
                .build();
    }
}
