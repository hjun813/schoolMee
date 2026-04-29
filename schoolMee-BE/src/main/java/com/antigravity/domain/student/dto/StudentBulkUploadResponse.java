package com.antigravity.domain.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentBulkUploadResponse {
    private int totalUploaded;
    private List<StudentProfileDto> createdStudents;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentProfileDto {
        private Long studentId;
        private String name;
        private String faceKey;
    }
}
