package com.antigravity.domain.photo.repository;

import com.antigravity.domain.photo.entity.PipelineJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineJobRepository extends JpaRepository<PipelineJob, Long> {
}
