package com.antigravity.domain.story.controller;

import com.antigravity.domain.story.dto.StoryGenerateResponse;
import com.antigravity.domain.story.dto.StoryListResponse;
import com.antigravity.domain.story.dto.StoryResponse;
import com.antigravity.domain.story.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "03. Story", description = "AI 스토리 생성 및 조회")
public class StoryController {

    private final StoryService storyService;

    @Operation(summary = "학교 전체 학생 AI 스토리 일괄 생성",
               description = "학교의 사진 풀을 기반으로 모든 학생의 Story + Chapter + Photo를 자동 생성합니다. 이미 스토리가 있는 학생은 건너뜁니다.")
    @PostMapping("/api/v1/admin/schools/{schoolId}/stories/generate")
    public ResponseEntity<StoryGenerateResponse> generateStories(
            @Parameter(description = "학교 ID") @PathVariable(name = "schoolId") Long schoolId) {
        return ResponseEntity.ok(storyService.generateStoriesForSchool(schoolId));
    }

    @Operation(summary = "학교 전체 스토리 목록 조회",
               description = "학교에 속한 모든 학생의 스토리 현황을 반환합니다. 관리자 검수 화면에서 사용합니다.")
    @GetMapping("/api/v1/admin/schools/{schoolId}/stories")
    public ResponseEntity<StoryListResponse> getStoriesBySchool(
            @Parameter(description = "학교 ID") @PathVariable(name = "schoolId") Long schoolId) {
        return ResponseEntity.ok(storyService.getStoriesBySchool(schoolId));
    }

    @Operation(summary = "학생별 스토리 상세 조회",
               description = "특정 학생의 Story → Chapter → Photo 전체 구조를 반환합니다. 개별 앨범 내용 검수 시 사용합니다.")
    @GetMapping("/api/v1/admin/students/{studentId}/stories")
    public ResponseEntity<List<StoryResponse>> getStoriesByStudent(
            @Parameter(description = "학생 ID") @PathVariable(name = "studentId") Long studentId) {
        return ResponseEntity.ok(storyService.getStoriesByStudent(studentId));
    }
}
