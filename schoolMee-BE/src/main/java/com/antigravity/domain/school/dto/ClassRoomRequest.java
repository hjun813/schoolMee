package com.antigravity.domain.school.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClassRoomRequest {
    private Integer grade;
    private Integer classNum;
}
