package com.antigravity.domain.photo.dto;

import com.antigravity.domain.photo.entity.PipelineJob;
import com.antigravity.domain.photo.entity.PipelineJobStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PipelineStatusResponse {
    private Long jobId;
    private Long schoolId;
    private PipelineJobStatus status;
    private Integer processedPhotos;
    private Integer storiesGenerated;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;

    public static PipelineStatusResponse from(PipelineJob job) {
        return PipelineStatusResponse.builder()
                .jobId(job.getId())
                .schoolId(job.getSchoolId())
                .status(job.getStatus())
                .processedPhotos(job.getProcessedPhotos())
                .storiesGenerated(job.getStoriesGenerated())
                .startTime(job.getStartTime())
                .endTime(job.getEndTime())
                .errorMessage(job.getErrorMessage())
                .build();
    }
}
