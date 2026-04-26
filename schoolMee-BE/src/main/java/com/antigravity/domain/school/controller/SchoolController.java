package com.antigravity.domain.school.controller;

import com.antigravity.domain.photo.dto.PhotoPoolResponse;
import com.antigravity.domain.school.dto.SchoolDashboardResponse;
import com.antigravity.domain.school.service.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/schools")
@RequiredArgsConstructor
@Tag(name = "01. School", description = "학교 대시보드 및 사진 풀 관리")
public class SchoolController {

    private final SchoolService schoolService;

    @Operation(summary = "학교 대시보드 조회",
               description = "학교 기본 정보 + 학생/스토리/주문 진행 현황 통계를 반환합니다. 대시보드 진입 시 첫 번째로 호출합니다.")
    @GetMapping("/{schoolId}")
    public ResponseEntity<SchoolDashboardResponse> getDashboard(
            @Parameter(description = "학교 ID") @PathVariable(name = "schoolId") Long schoolId) {
        return ResponseEntity.ok(schoolService.getDashboard(schoolId));
    }

    @Operation(summary = "학교 사진 풀 조회",
               description = "해당 학교에 업로드된 사진 목록과 AI 분석 점수를 반환합니다. 스토리 생성 전 사진 현황을 확인할 때 사용합니다.")
    @GetMapping("/{schoolId}/photos")
    public ResponseEntity<PhotoPoolResponse> getPhotoPool(
            @Parameter(description = "학교 ID") @PathVariable(name = "schoolId") Long schoolId) {
        return ResponseEntity.ok(schoolService.getPhotoPool(schoolId));
    }
}
