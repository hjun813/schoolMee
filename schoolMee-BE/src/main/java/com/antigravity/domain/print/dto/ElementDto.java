package com.antigravity.domain.print.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ElementDto {
    private String type; // "text" or "image"
    private String contentOrUrl; // Text value or image URL
    private Integer x;
    private Integer y;
    private Integer width;  // only valid for "image"
    private Integer height; // only valid for "image"
}
