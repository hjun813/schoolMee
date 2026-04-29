package com.antigravity.domain.print.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PageDto {
    private int pageNumber;
    private String type; // "cover" or "chapter"
    private List<ElementDto> elements;
}
