package com.antigravity.domain.school.repository;

import com.antigravity.domain.school.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
}
