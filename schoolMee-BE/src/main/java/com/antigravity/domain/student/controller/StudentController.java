package com.antigravity.domain.student.controller;

import com.antigravity.domain.student.dto.StudentDetailResponse;
import com.antigravity.domain.student.dto.StudentListResponse;
import com.antigravity.domain.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "02. Student", description = "학생 목록 및 상세 조회")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "학교 소속 학생 목록 조회",
               description = "학교에 속한 학생 목록을 반환합니다. hasStory/hasOrder 플래그로 버튼 활성화 여부를 판단할 수 있습니다.")
    @GetMapping("/api/v1/admin/schools/{schoolId}/students")
    public ResponseEntity<StudentListResponse> getStudents(
            @Parameter(description = "학교 ID") @PathVariable(name = "schoolId") Long schoolId) {
        return ResponseEntity.ok(studentService.getStudentsBySchool(schoolId));
    }

    @Operation(summary = "학생 상세 조회",
               description = "특정 학생의 정보와 스토리 요약(챕터 수, 사진 수)을 반환합니다.")
    @GetMapping("/api/v1/admin/students/{studentId}")
    public ResponseEntity<StudentDetailResponse> getStudentDetail(
            @Parameter(description = "학생 ID") @PathVariable(name = "studentId") Long studentId) {
        return ResponseEntity.ok(studentService.getStudentDetail(studentId));
    }
}
