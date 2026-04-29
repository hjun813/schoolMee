package com.antigravity.domain.print.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PrintPackageResponse {
    private Long orderId; // 연관된 OrderId가 없을경우 null
    private Long storyId;
    private String studentName;
    private String summary;
    private List<PageDto> pages;
}
