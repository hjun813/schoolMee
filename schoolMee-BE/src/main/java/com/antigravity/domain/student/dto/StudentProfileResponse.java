package com.antigravity.domain.student.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentProfileResponse {
    private Long studentId;
    private String faceKey;
    private String profileImagePath;
    private String message;
}
