package com.antigravity.domain.student.dto;

import com.antigravity.domain.student.entity.Student;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentResponse {
    private Long id;
    private String name;
    private Integer grade;
    private Integer classNum;
    private String schoolName;

    public static StudentResponse from(final Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .grade(student.getGrade())
                .classNum(student.getClassNum())
                .schoolName(student.getSchool().getName())
                .build();
    }
}
