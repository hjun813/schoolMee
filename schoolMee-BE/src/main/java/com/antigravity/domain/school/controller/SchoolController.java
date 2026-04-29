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

    @Operation(summary = "학교 목록 조회", description = "시스템에 등록된 모든 학교의 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<java.util.List<SchoolDashboardResponse>> getAllSchools() {
        return ResponseEntity.ok(schoolService.getAllSchools());
    }

    @Operation(summary = "학교 등록 (온보딩 1단계)", description = "새로운 학교를 보드에 등록합니다. 등록 후 온보딩 상태가 SCHOOL_CREATED가 됩니다.")
    @PostMapping
    public ResponseEntity<Long> createSchool(@RequestBody java.util.Map<String, String> request) {
        return ResponseEntity.ok(schoolService.createSchool(request.get("name")));
    }

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
    @Operation(summary = "반(ClassRoom) 생성",
               description = "학교 내 새로운 반을 생성합니다. 온보딩 2단계에 해당하며, 생성 후 온보딩 상태가 CLASS_CREATED로 변경됩니다.")
    @PostMapping("/{schoolId}/classes")
    public ResponseEntity<Long> createClassRoom(
            @Parameter(description = "학교 ID") @PathVariable(name = "schoolId") Long schoolId,
            @RequestBody com.antigravity.domain.school.dto.ClassRoomRequest request) {
        return ResponseEntity.ok(schoolService.createClassRoom(schoolId, request));
    }
}
