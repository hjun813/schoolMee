package com.antigravity.domain.photo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_jobs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PipelineJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PipelineJobStatus status;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "processed_photos")
    private Integer processedPhotos;

    @Column(name = "stories_generated")
    private Integer storiesGenerated;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public void updateStatus(PipelineJobStatus status) {
        this.status = status;
        if (status == PipelineJobStatus.PROCESSING) {
            this.startTime = LocalDateTime.now();
        } else if (status == PipelineJobStatus.SUCCEEDED || status == PipelineJobStatus.FAILED) {
            this.endTime = LocalDateTime.now();
        }
    }

    public void complete(int processedPhotos, int storiesGenerated) {
        this.status = PipelineJobStatus.SUCCEEDED;
        this.processedPhotos = processedPhotos;
        this.storiesGenerated = storiesGenerated;
        this.endTime = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = PipelineJobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();
    }
}
